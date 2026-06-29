package cn.bctools.document.controller;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.TreeUtils;
import cn.bctools.document.auth.dto.DocumentAuthDto;
import cn.bctools.document.auth.util.AuthSystemTool;
import cn.bctools.document.component.AsyncComponent;
import cn.bctools.document.component.UserComponent;
import cn.bctools.document.config.CommonConfig;
import cn.bctools.document.constant.Constant;
import cn.bctools.document.dto.*;
import cn.bctools.document.entity.*;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryReadEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.IdentifyingKeyEnum;
import cn.bctools.document.listener.es.DocumentMqEvent;
import cn.bctools.document.log.DocumentLog;
import cn.bctools.document.message.aspect.MessagePush;
import cn.bctools.document.office.OfficeContentUtil;
import cn.bctools.document.receiver.DocumentConsumer;
import cn.bctools.document.service.*;
import cn.bctools.document.util.FileOssUtils;
import cn.bctools.document.vo.req.*;
import cn.bctools.document.vo.res.DocumentSearchResVo;
import cn.bctools.document.vo.res.PreviewDocumentResVo;
import cn.bctools.log.annotation.Log;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 知识库操作
 *
 * @author guojing
 */
@Slf4j
@Api(tags = "知识库")
@RestController
@RequestMapping(value = "/dcLibrary")
@AllArgsConstructor
public class DcLibraryController {

    OssTemplate ossTemplate;
    UserComponent userComponent;
    DcTagService dcTagService;
    DcTagBindingService dcTagBindingService;

    DcLibraryService dcLibraryService;
    DcTemplateService dcTemplateService;
    DcLibraryCollectService collectService;
    DocumentElasticService documentElasticService;
    CommonConfig commonConfig;
    AsyncComponent asyncComponent;
    DcAuthConfigService dcAuthConfigService;
    DocumentConsumer documentConsumer;
    DcShareService dcShareService;
    AuthUserServiceApi authUserServiceApi;
    DcLibraryCollectService dcLibraryCollectService;


    @Log
    @ApiOperation("我的收藏集合ID")
    @GetMapping("/collectAll")
    public R<List<String>> collectAll() {
        List<String> docIds = collectService.getCollection();
        return R.ok(docIds);
    }


    @Log
    @PostMapping("/office/add")
    @ApiOperation("新建数据")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.ADD)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.ADD, returnValueIs = true)
    public R<DcLibrary> add(@RequestBody OfficeAddDto officeAddDto) {
//        //判断名称是否重复
//        if (dcLibraryService.checkRepetition(officeAddDto.getParentId(), officeAddDto.getName(), reqVo.getFileType(), Boolean.TRUE)) {
//            return R.failed("文档名称重复");
//        }
        BaseFile office = OfficeContentUtil.createOffice(officeAddDto.getType());
        //如果文档库id不存在表示是在onlyoffice 编辑页面新增的
        DcLibrary byId = dcLibraryService.getById(officeAddDto.getParentId());
        log.info("新增office文档时传入的数据为{}", JSONUtil.toJsonStr(officeAddDto));
        DcLibrary one = dcLibraryService.getOne(Wrappers.<DcLibrary>lambdaQuery().select(DcLibrary::getOrderId).eq(DcLibrary::getParentId, officeAddDto.getParentId()).orderByDesc(DcLibrary::getOrderId).last("limit 1 "));
        int orderId = ObjectUtil.isNull(one) ? 0 : one.getOrderId();
        //获取上级数组
        List<String> pathId = byId.getPathId();
        pathId.add(byId.getId());
        DcLibrary dcLibrary = new DcLibrary().setName(office.getOriginalName()).setNameSuffix(FileUtil.getSuffix(office.getOriginalName())).setKnowledgeId(officeAddDto.getKnowledgeId()).setParentId(officeAddDto.getParentId()).setOrderId(orderId + 1).setPathId(pathId).setBucketName(office.getBucketName()).setFilePath(office.getFileName()).setType(DcLibraryTypeEnum.suffixToEnum(officeAddDto.getType()));
        dcLibraryService.save(dcLibrary);
        return R.ok(dcLibrary);
    }

    @Log
    @ApiOperation("根据标签查询所有文档分页数据")
    @GetMapping("/library/page/{label}")
    public R<Page<DcLibrary>> labelDcLibrary(Page<DcLibrary> page, @PathVariable String label) {
        dcLibraryService.page(page, new LambdaQueryWrapper<DcLibrary>().like(DcLibrary::getLabel, label));
        return R.ok(page);
    }

    @Log
    @ApiOperation("回收站")
    @GetMapping("/recycle")
    public R<Page<DcLibrary>> recycle(Page<DcLibrary> page) {
        dcLibraryService.recycle(page);
        return R.ok(page);
    }

    @Log
    @ApiOperation("清空回收站")
    @GetMapping("/clear/recycle")
    public R<Boolean> clearRecycle() {
        asyncComponent.asyncClearRecycle();
        return R.ok(Boolean.TRUE);
    }

    @Log
    @ApiOperation("恢复")
    @PostMapping("/retrieve")
    public R<List<String>> retrieve(@RequestBody List<DcLibrary> dcLibrary) {
        List<String> list = new ArrayList<>();
        dcLibrary.forEach(e -> {
            try {
                dcLibraryService.retrieve(e);
            } catch (Exception exception) {
                list.add(e.getName());
                log.error("恢复错误:", exception);
            }
        });
        return R.ok(list);
    }

    @Log
    @ApiOperation("置顶")
    @PostMapping("/top/{id}")
    public R<Boolean> top(@PathVariable String id) {
        dcLibraryService.update(new UpdateWrapper<DcLibrary>().lambda().set(DcLibrary::getTopTime, LocalDateTime.now()).eq(DcLibrary::getId, id));
        return R.ok(Boolean.TRUE);
    }

    @Log
    @ApiOperation("清除置顶")
    @PostMapping("/clear/top/{id}")
    public R<Boolean> clearTop(@PathVariable String id) {
        dcLibraryService.update(new UpdateWrapper<DcLibrary>().lambda().set(DcLibrary::getTopTime, null).eq(DcLibrary::getId, id));
        return R.ok(Boolean.TRUE);
    }


    @Log
    @ApiOperation("添加知识库、目录、文件")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.ADD)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.ADD, returnValueIs = true)
    public R<DcLibrary> add(@RequestBody @Validated DcLibraryAddReqVo reqVo) throws IOException {
        //判断名称是否重复
//        if (dcLibraryService.checkRepetition(reqVo.getId(), reqVo.getName(), reqVo.getFileType(), Boolean.TRUE)) {
//            return R.failed("文档名称重复");
//        }
        String templateId = reqVo.getTemplateId();
        String content = "";
        boolean useTemplate = StringUtils.isNotBlank(templateId);
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        if (useTemplate) {
            // 通过模板创建
            DcTemplate template = dcTemplateService.get(templateId);
            if (StrUtil.isEmpty(template.getBucketName()) || StrUtil.isEmpty(template.getBucketName())) {
                throw new BusinessException("该模板未设计无法使用");
            }
            reqVo.setName(template.getName());
            reqVo.setFileType(template.getType());
            if (DcLibraryTypeEnum.document_unrecognized.equals(template.getType())) {
                return R.failed("不能操作未知文件模板");
            }
            //office文件
            if (Arrays.asList(DcLibraryTypeEnum.office_doc, DcLibraryTypeEnum.office_ppt, DcLibraryTypeEnum.office_xlsx, DcLibraryTypeEnum.office_pdf, DcLibraryTypeEnum.office_csv).contains(template.getType())) {
                String fileLink = ossTemplate.fileLink(template.getFilePath(), template.getBucketName());
                InputStream inputStream = FileOssUtils.getInputStream(fileLink);
                //上传文件
                DcLibrary dcLibrary = dcLibraryService.add(currentUser, reqVo);
                //设置后缀名
                dcLibrary.setNameSuffix(template.getNameSuffix());
                String originalName = FileOssUtils.getMultipartFileName(dcLibrary) + "." + dcLibrary.getNameSuffix();
                DcLibrary know = dcLibraryService.get(dcLibrary.getKnowledgeId());
                BaseFile baseFile = ossTemplate.putFile(originalName, inputStream, know.getName(), dcLibrary.getName());
                //修改
                //设置文件存储位置
                dcLibrary.setBucketName(baseFile.getBucketName()).setFilePath(baseFile.getFileName()).setSize(baseFile.getSize());
                dcLibraryService.updateById(dcLibrary);
                //入队列
                DocumentMqEvent documentMqEvent = new DocumentMqEvent().setDocumentId(dcLibrary.getId()).setContent(dcLibrary.getContent()).setTenantId(TenantContextHolder.getTenantId()).setUserDto(currentUser);
                documentConsumer.sendFileToEs(documentMqEvent);
                inputStream.close();
                return R.ok(dcLibrary);
            }
            String fileLink = ossTemplate.fileLink(template.getFilePath(), template.getBucketName());
            byte[] bytes = HttpUtil.downloadBytes(fileLink);
            content = bytes == null ? null : ObjectUtil.deserialize(bytes);
            if (StrUtil.isNotBlank(template.getOtherJson())) {
                JSONObject jsonObject = JSONObject.parseObject(content);
                jsonObject.put(Constant.OTHER_JSON_WEB, template.getOtherJson());
                content = jsonObject.toString();
            }
        }
        DcLibrary dcLibrary = dcLibraryService.add(currentUser, reqVo);
        if (useTemplate) {
            //判断是否存在其他参数
            this.saveContent(content, dcLibrary.getId());
        } else if (reqVo.getFileType() != null && !reqVo.getFileType().equals(DcLibraryTypeEnum.url_address)) {
            dcLibrary.setTenantId(TenantContextHolder.getTenantId());
            // 保存到文档es
            documentElasticService.save(currentUser, dcLibrary, "");
        }
        return R.ok(dcLibrary);
    }

    /**
     * 重命名 知识库 目录 文档
     * 修改设置
     */
    @Log
    @PutMapping
    @ApiOperation("重命名知识库/目录/文档，或设置知识库")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.UPDATE)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.UPDATE)
    public R<DcLibrary> update(@RequestBody @Validated DcLibraryEditReqVo dto) {
        //判断名称是否重复
//        if (dcLibraryService.checkRepetition(dto.getId(), dto.getName(), dto.getType(), Boolean.FALSE)) {
//            return R.failed("文档名称重复");
//        }
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        DcLibrary dcLibrary = dcLibraryService.put(currentUser, dto);
        if (DcLibraryTypeEnum.isFile(dto.getType())) {
            // 修改文档es
            documentElasticService.updateDocumentEs(dcLibrary);
        }
        return R.ok(dcLibrary);
    }

    /**
     * 重命名 知识库 目录 文档
     * 修改设置
     */
    @Log
    @PutMapping("/update/name")
    @ApiOperation("文档编辑页重命名")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.UPDATE)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.UPDATE)
    public R<DcLibrary> updateName(@RequestBody DcLibrary dcLibraryUpdate) {
        //判断名称是否重复
//        if (dcLibraryService.checkRepetition(dcLibraryUpdate.getId(), dcLibraryUpdate.getName(), dcLibraryUpdate.getType(), Boolean.FALSE)) {
//            return R.failed("文档名称重复");
//        }
        DcLibrary dcLibrary = dcLibraryService.getById(dcLibraryUpdate.getId());
        dcLibrary.setName(dcLibraryUpdate.getName());
        dcLibraryService.update(new UpdateWrapper<DcLibrary>().lambda().eq(DcLibrary::getId, dcLibrary.getId()).set(DcLibrary::getName, dcLibrary.getName()));
        // 修改文档es
        documentElasticService.updateDocumentEs(dcLibrary);
        return R.ok(dcLibrary);
    }


    @Log
    @PutMapping("/location/other")
    @ApiOperation("移动文档")
    @Transactional(rollbackFor = Exception.class)
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.MOVE)
    public R<Boolean> locationOther(@RequestBody DcLocationOtherDto dcLocationOtherDto) {
        dcLibraryService.locationOther(dcLocationOtherDto);
        return R.ok(true);
    }

    @Log
    @PostMapping("/physical/delete")
    @ApiOperation("完全删除")
    @Transactional(rollbackFor = Exception.class)
    public R<String> delete(@RequestBody List<String> ids) {
        ids.forEach(e -> dcLibraryService.removeDc(e));
        return R.ok();
    }

    @SneakyThrows
    @ApiOperation("复制文档")
    @PostMapping("/copy")
    public R<DcLibrary> copy(@RequestBody DcLibraryCopyDto dcLibraryCopyDto) {
        //获取源文件信息
        DcLibrary byId = dcLibraryService.getById(dcLibraryCopyDto.getId());
        byId.setName(dcLibraryCopyDto.getName());
        byId.setId(null).setCreateTime(null);
        byId.setUpdateTime(null);
        byId.setUpdateBy(null);
        byId.setCreateById(null);
        byId.setCreateBy(null);
        DcLibrary parent = dcLibraryService.getOne(Wrappers.<DcLibrary>lambdaQuery()
                .select(DcLibrary::getOrderId, DcLibrary::getPathId, DcLibrary::getType)
                .eq(DcLibrary::getParentId, byId.getParentId())
                .orderByDesc(DcLibrary::getOrderId)
                .last("limit 1"));
        byId.setOrderId(parent.getOrderId() + 1);
        dcLibraryService.save(byId);
        return R.ok(byId);
    }

    @Log
    @DeleteMapping("/recycle/{id}")
    @ApiOperation("移到回收站")
    @Transactional(rollbackFor = Exception.class)
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.DELETE)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.DELETE)
    public R<DcLibrary> recycle(@PathVariable String id) {
        DcLibrary dcLibrary = dcLibraryService.recycleDc(id);
        return R.ok(dcLibrary);
    }

    @Log
    @DeleteMapping("/clear/auth")
    @ApiOperation("清空当前文库下面的定制权限")
    public R clearAuth(@PathVariable String id) {
        dcLibraryService.clearAuth(id);
        return R.ok();
    }

    @Log
    @GetMapping("/tree")
    @ApiOperation("获取目录树")
    public R<TreeAuthDto> tree() {
        List<DcLibrary> tree = dcLibraryService.currentLevel();
        String id = AuthSystemTool.getDocumentAuth().getId();
        log.info("---------------------获取线程中的数据为:{}", JSONObject.toJSONString(AuthSystemTool.getDocumentAuth()));
        //当前目录用户拥有的权限
        List<IdentifyingAuthDto> identifying = AuthSystemTool.getDocumentAuth().getIdentifyingRoleMap().get(id);
        TreeAuthDto treeAuthDto = new TreeAuthDto();
        //获取当前目录路径
        DcLibrary byId = dcLibraryService.getById(id);
        //置顶数据
        List<DcLibrary> topList = new ArrayList<>();
        //获取文库信息
        DcLibrary know = byId;
        if (!byId.getType().equals(DcLibraryTypeEnum.knowledge)) {
            know = dcLibraryService.getById(byId.getKnowledgeId());
        } else {
            //私有文库没有推荐
            DcLibraryReadEnum shareRole = byId.getShareRole();
            if (!shareRole.equals(DcLibraryReadEnum.user)) {
                topList = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().eq(DcLibrary::getKnowledgeId, byId.getId()).isNotNull(DcLibrary::getTopTime).orderByAsc(DcLibrary::getTopTime));
            }
        }
        //获取置顶数据
        //通过所有者id 获取用户信息
        UserDto data = authUserServiceApi.getById(know.getPossessor()).getData();
        know.setPossessorName(data.getRealName());
        treeAuthDto.setKnowledge(know);
        //没有上级就不用获取
        List<DcLibrary> list = new ArrayList<>();
        if (!byId.getPathId().isEmpty()) {
            list = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getId, DcLibrary::getName, DcLibrary::getType, DcLibrary::getPathId).in(DcLibrary::getId, byId.getPathId())).stream().sorted(Comparator.comparingInt(e -> e.getPathId().size())).collect(Collectors.toList());
        }
        treeAuthDto.setDirectoryStructure(list);
        if (tree.isEmpty()) {
            treeAuthDto.setData(new ArrayList<>()).setDcIdentifying(identifying).setDcLibrary(byId);
            return R.ok(treeAuthDto);
        }
        treeAuthDto.setDcIdentifying(identifying).setDcLibrary(byId);
        //获取文档数量
        treeAuthDto.setCount(tree.size());
        //获取是否分享
        List<String> ids = tree.stream().map(DcLibrary::getId).collect(Collectors.toList());
        Map<String, Boolean> map = dcShareService.list(new LambdaQueryWrapper<DcShareEntity>().in(DcShareEntity::getDcId, ids)).stream().collect(Collectors.toMap(DcShareEntity::getDcId, DcShareEntity::getShare));
        if (!map.isEmpty()) {
            tree.stream().peek(e -> e.setShare(Optional.ofNullable(map.get(e.getId())).orElse(Boolean.FALSE))).collect(Collectors.toList());
        }
        //获取是否收藏
        List<String> collect = dcLibraryCollectService.list(new LambdaQueryWrapper<DcLibraryCollect>().in(DcLibraryCollect::getDocId, ids).eq(DcLibraryCollect::getUserId, UserCurrentUtils.getUserId())).stream().map(DcLibraryCollect::getDocId).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            tree.stream().peek(e -> e.setCollectIs(collect.contains(e.getId()))).collect(Collectors.toList());
        }
        List<DcLibrary> libraryList = new ArrayList<>();
        libraryList.add(new DcLibrary().setId("-1"));
        treeAuthDto.setData(tree);
        treeAuthDto.setTopList(topList);
        return R.ok(treeAuthDto);
    }


    @Log
    @GetMapping("/get/tree/name")
    @ApiOperation("通过节点id获取 此节点的所有数据")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "文档id")})
    public R<List<String>> getTree(String id) {
        return R.ok(dcLibraryService.getTreeName(id));
    }

    @Log
    @GetMapping("/tree/folder")
    @ApiOperation("获取文件夹树形结构")
    public R<List<DcLibrary>> treeFolder() {
        List<DcAuthConfig> authConfig = dcAuthConfigService.getAuthConfig(IdentifyingKeyEnum.document_add);
        if (authConfig.isEmpty()) {
            return R.ok(new ArrayList<>());
        }
        List<String> ids = authConfig.stream().filter(e -> e.getType().equals(DcLibraryTypeEnum.knowledge)).map(DcAuthConfig::getDcId).collect(Collectors.toList());
        //获取可以操作的数据
        List<DcLibrary> list = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getNameSuffix, DcLibrary::getId, DcLibrary::getKnowledgeId, DcLibrary::getName, DcLibrary::getType, DcLibrary::getParentId, DcLibrary::getOrderId).in(DcLibrary::getType, DcLibraryTypeEnum.knowledge, DcLibraryTypeEnum.directory).and(e -> e.in(DcLibrary::getKnowledgeId, ids).or().in(DcLibrary::getId, ids)));
        list = list.stream().filter(e -> e.getType().equals(DcLibraryTypeEnum.directory) || e.getType().equals(DcLibraryTypeEnum.knowledge)).collect(Collectors.toList());
        //获取根节点数据
        List<String> rootKey = list.parallelStream().filter(e -> e.getType().equals(DcLibraryTypeEnum.knowledge)).map(DcLibrary::getId).collect(Collectors.toList());
        Function<DcLibrary, String> getId = DcLibrary::getId;
        list = TreeUtils.list2Tree(list, rootKey, getId, DcLibrary::getParentId, DcLibrary::setChildren);
        //设置权限标识
        return R.ok(list);
    }


    @SneakyThrows
    @PostMapping("/save/content/{documentId}")
    @ApiOperation("保存文档")
    @DocumentLog
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.UPDATE)
    public R<String> saveContent(@RequestBody String content, @PathVariable String documentId) {
        //数据拆分
        String otherJson = null;
        if (content.contains(Constant.OTHER_JSON_WEB)) {
            JSONObject jsonObject = JSONObject.parseObject(content);
            if (jsonObject.containsKey(Constant.OTHER_JSON_WEB)) {
                otherJson = jsonObject.getString(Constant.OTHER_JSON_WEB);
                jsonObject.remove(Constant.OTHER_JSON_WEB);
                content = jsonObject.toJSONString();
            }
        }
        DcLibrary dcLibrary = dcLibraryService.get(documentId);
        DcLibraryTypeEnum type = dcLibrary.getType();
        if (DcLibraryTypeEnum.knowledge.equals(type)) {
            return R.failed("文档类型异常");
        }
        // 获取该文档的文件名称
        String originalName = FileOssUtils.getMultipartFileName(dcLibrary);
        // 将知识库名称当做文件的一级目录
        DcLibrary know = dcLibraryService.get(dcLibrary.getKnowledgeId());
        BaseFile baseFile = ossTemplate.putContent(originalName, content, know.getName(), dcLibrary.getName());
        dcLibrary.setBucketName(baseFile.getBucketName());
        dcLibrary.setSize(baseFile.getSize());
        dcLibrary.setFilePath(baseFile.getFileName()).setOtherJson(otherJson);
        UserDto currentUser = UserCurrentUtils.getCurrentUser();
        dcLibraryService.saveContent(currentUser.getId(), dcLibrary, documentId);
        //同步ES
        try {
            // 保存到文档es
            documentElasticService.save(currentUser, dcLibrary, content);
        } catch (Exception e) {
            log.error("同步ES错误：{}", e.getMessage());
            throw new BusinessException("保存失败");
        }
        return R.ok("保存成功");
    }


    @SneakyThrows
    @ApiOperation("分片上传绑定数据")
    @PostMapping("/save/file/{id}")
    @Transactional(rollbackFor = Exception.class)
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.ADD)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.ADD, returnValueIs = true)
    public R<DcLibrary> saveFile(@PathVariable("id") String id, @RequestBody BaseFile baseFile) {
        DcLibrary dcLibrary = dcLibraryService.fileToSave(id, baseFile, null);
        return R.ok(dcLibrary);
    }


    @SneakyThrows
    @ApiOperation("上传文档")
    @PostMapping("/upload/{id}")
    @Transactional(rollbackFor = Exception.class)
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.ADD)
    @MessagePush(messagePushTye = DcLibraryLogOperationTypeEnum.ADD, returnValueIs = true)
    public R<DcLibrary> upload(@PathVariable("id") String id, @RequestParam("file") MultipartFile file) {
        //查询点击的当前元素
        String applicationContextName = SpringContextUtil.getApplicationContextName();
        BaseFile baseFile = ossTemplate.putFile(SpringContextUtil.getApplicationContextName(), applicationContextName, file.getOriginalFilename(), file);
        return saveFile(id, baseFile);
    }

    @SneakyThrows
    @GetMapping("/preview/document/{id}")
    @ApiOperation("预览文档")
    @DocumentLog(operationType = DcLibraryLogOperationTypeEnum.SEE)
    public R<PreviewDocumentResVo> preview(@PathVariable("id") String id) {
        // 帮助文档不鉴权
        DcLibrary library = dcLibraryService.getById(id);
        if (library == null) {
            throw new BusinessException("数据不存在");
        }
        //获取权限判断当前用户是否可以查看
        DocumentAuthDto dataScope = AuthSystemTool.getDocumentAuth();
        if (!dataScope.getIds().contains(library.getId())) {
            return R.failed("权限不足请联系文档所有者");
        }
        // 非帮助文档需校验查询权限
        return R.ok(dcLibraryService.preview(library, UserCurrentUtils.getCurrentUser()));
    }


    @Log
    @ApiOperation("搜索文档")
    @GetMapping("/document/search")
    public R<Page<DocumentSearchResVo>> searchDoc(Page page, DocumentSearchVo documentSearchVo) {
        Page<DocumentSearchResVo> data;
        try {
            data = documentElasticService.searchDoc(page, documentSearchVo);
        } catch (BusinessException exception) {
            return R.failed(exception.getMessage());
        }
        //设置后缀名
        if (data.getTotal() > BigDecimal.ROUND_UP) {
            List<String> ids = data.getRecords().parallelStream().map(DocumentSearchResVo::getDocId).distinct().collect(Collectors.toList());
            Map<String, String> map = dcLibraryService.listByIds(ids).parallelStream().filter(e -> StrUtil.isNotBlank(e.getNameSuffix())).collect(Collectors.toMap(DcLibrary::getId, DcLibrary::getNameSuffix));
            data.getRecords().parallelStream().peek(e -> e.setNameSuffix(map.getOrDefault(e.getDocId(), null))).collect(Collectors.toList());
        }

        //设置标签
        List<String> dcIds = data.getRecords().stream().map(DocumentSearchResVo::getDocId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(dcIds)) {
            List<DcTagBinding> bindings = dcTagBindingService.list(Wrappers.lambdaQuery(DcTagBinding.class).in(DcTagBinding::getDcId, dcIds));
            Map<String, List<DcTagBinding>> bindingMap = bindings.stream().collect(Collectors.groupingBy(DcTagBinding::getDcId));
            List<String> tagIds = bindings.stream().map(DcTagBinding::getTagId).distinct().collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(tagIds)) {
                Map<String, DcTag> tagMap = dcTagService.listByIds(tagIds).stream().collect(Collectors.toMap(DcTag::getId, Function.identity()));
                data.getRecords().stream().peek(e -> {
                    List<DcTagBinding> binding2Doc = bindingMap.getOrDefault(e.getDocId(), Collections.emptyList());
                    if (CollectionUtil.isNotEmpty(binding2Doc)) {
                        List<DcTag> tags = binding2Doc.stream().map(o -> tagMap.get(o.getTagId())).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
                        e.setTags(tags);
                    }
                }).collect(Collectors.toList());
            }
        }

        return R.ok(data);
    }


    @Log
    @ApiOperation("查询用户有权限的知识库")
    @GetMapping("/knowledges")
    public R<Page<DcLibrary>> findKnowledge(Page<DcLibrary> page, DcLibrary dcLibrary) {
        return R.ok(dcLibraryService.queryKnowledge(page, dcLibrary));
    }

    @Log
    @ApiOperation("查询用户自己创建的知识库")
    @GetMapping("/knowledges/owner")
    public R<Page<DcLibrary>> findOwnerKnowledge(Page<DcLibrary> page, DcLibrary dcLibrary) {
        return R.ok(dcLibraryService.queryOwnerKnowledge(page, dcLibrary));
    }


    @Log
    @ApiOperation("清空搜索记录")
    @GetMapping("/clear/search")
    public R<Boolean> clearSearch() {
        UserDto user = UserCurrentUtils.getCurrentUser();
        documentElasticService.deleteLog(user.getTenantId(), user.getId());
        return R.ok(Boolean.TRUE);
    }


    @Log
    @ApiOperation(value = "获取知识库信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "id（知识库id、目录id、文档id）", value = "id", required = true)})
    @GetMapping("/info/{id}")
    public R<DcLibrary> queryDcLibraryInfo(@PathVariable String id) {
        log.info("======= 进入 /dcLibrary/info/{} 接口，参数 id: {}", id, id);
        // 查询
        DocumentAuthDto documentAuth = AuthSystemTool.getDocumentAuth();
        Set<String> ids = documentAuth.getIds();
        log.info("======= 当前用户权限 ids: {}", ids);
        if (ids.isEmpty() || !ids.contains(id)) {
            log.warn("======= 没有此文档操作权限，请求 id: {}", id);
            return R.failed("没有此文档操作权限");
        }
        DcLibrary dcLibraryInfo = dcLibraryService.getById(id);
        if (dcLibraryInfo == null) {
            log.warn("======= 资源不存在，请求 id: {}", id);
            return R.failed("资源不存在");
        }
        log.info("======= /dcLibrary/info/{} 接口返回数据: {}", id, JSONObject.toJSONString(dcLibraryInfo));
        return R.ok(dcLibraryInfo.setDcIdentifying(documentAuth.getIdentifyingRoleMap().get(id)));
    }


    @Log
    @ApiOperation(value = "设置文档查看提醒")
    @PutMapping("/notify/read/setting")
    public R<String> settingReadNotify(@RequestBody @Validated SettingReadNotifyReqVo reqVo) {
        // 数据校验
        DcLibrary dcLibrary = dcLibraryService.getById(reqVo.getId());
        if (dcLibrary == null) {
            throw new BusinessException("知识库文档不存在");
        }
        if (!DcLibraryTypeEnum.knowledge.equals(dcLibrary.getType())) {
            throw new BusinessException("只支持文档设置查看提醒开关");
        }
        // 保存设置
        dcLibrary.setReadNotify(reqVo.getReadNotify());
        dcLibraryService.updateById(dcLibrary);
        return R.ok();
    }

    @Log
    @ApiOperation(value = "重置排序")
    @PostMapping("/reset/sort")
    public R<Boolean> resetSort(@RequestBody ResetSortDto resetSortDto) {
        dcLibraryService.resetSort(resetSortDto);
        return R.ok(Boolean.TRUE);
    }
}
