package cn.bctools.document.service.impl;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.IdGenerator;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.TreeUtils;
import cn.bctools.common.utils.function.Get;
import cn.bctools.document.auth.dto.DocumentAuthDto;
import cn.bctools.document.auth.dto.LibraryAuthDto;
import cn.bctools.document.auth.util.AuthSystemTool;
import cn.bctools.document.config.CommonConfig;
import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.dto.ResetSortDto;
import cn.bctools.document.entity.*;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.entity.enums.SourceFileUdOperateStatusEnums;
import cn.bctools.document.listener.es.DocumentMqEvent;
import cn.bctools.document.mapper.DcLibraryMapper;
import cn.bctools.document.po.DocumentEsPo;
import cn.bctools.document.po.SourceFileDownFilePo;
import cn.bctools.document.po.SourceFileDownTypePo;
import cn.bctools.document.receiver.DocumentConsumer;
import cn.bctools.document.service.*;
import cn.bctools.document.util.*;
import cn.bctools.document.vo.OnlyOfficeFileTransitionVo;
import cn.bctools.document.vo.req.DcLibraryAddReqVo;
import cn.bctools.document.vo.req.DcLibraryEditReqVo;
import cn.bctools.document.vo.req.DcLocationOtherDto;
import cn.bctools.document.vo.res.PreviewDocumentResVo;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author auto
 */
@Slf4j
@Service
public class DcLibraryServiceImpl extends ServiceImpl<DcLibraryMapper, DcLibrary> implements DcLibraryService {
    @Autowired
    OssTemplate ossTemplate;
    @Autowired
    DocumentElasticService documentElasticService;
    @Autowired
    DcLibraryCollectService dcLibraryCollectService;
    @Autowired
    DcAuthConfigService dcAuthConfigService;
    @Autowired
    DcToppingService dcToppingService;
    @Autowired
    DocumentConsumer documentConsumer;
    @Autowired
    MessageConfigService messageConfigService;
    @Autowired
    AuthUserServiceApi authUserServiceApi;
    @Autowired
    SourceFileUdLogService sourceFileUdLogService;
    @Autowired
    DcLibraryRecycleService dcLibraryRecycleService;
    @Value("${oss.endpoint}")
    String ossUrl;
    @Autowired
    CommonConfig commonConfig;
    private final static String DOWN_FILE_PATH = "accessory";
    private final static String DOWN_ACCESSORY_JSON_NAME = "accessory.json";

    private final static String CONTENT_FILE_NAME = "content";


    @Override
    public void upSourceFile(BaseFile baseFile, String parentId, UserDto userDto,String logId) throws IOException {
        String errorMsg = null;
        String errorLog = null;
        String unzipPath = commonConfig.getUnzipPath();
        String separator = File.separator;
        try {
            FileUtil.mkdir(new File(unzipPath));
            String unzipPathFile = unzipPath + IdGenerator.getIdStr() + ".zip";
            //下载压缩包到本地
            String url = ossTemplate.fileLink(baseFile.getFileName(), baseFile.getBucketName());
            ZipUtil.downloadFile(url, unzipPathFile);
            //解压文件
            ZipUtil.unzip(unzipPath, unzipPathFile, commonConfig.getZipPwd());
            //读取内容文件
            String content = FileUtil.readUtf8String(unzipPath + CONTENT_FILE_NAME.concat(DcLibraryTypeEnum.document_html.value));
            //富文本关联关系
            String s = FileUtil.readUtf8String(unzipPath + separator + DOWN_ACCESSORY_JSON_NAME);
            List<SourceFileDownFilePo> list = JSONArray.parseArray(s, SourceFileDownFilePo.class);
            if (!list.isEmpty()) {
                for (SourceFileDownFilePo sourceFileDownFilePo : list) {
                    BufferedInputStream inputStream = FileUtil.getInputStream(commonConfig.getUnzipPath() + separator + sourceFileDownFilePo.getFielUrl());
                    BaseFile put = ossTemplate.put("jvs-public", "aomao", inputStream, FileUtil.extName(sourceFileDownFilePo.getFielUrl()), false);
                    content = content.replaceAll(sourceFileDownFilePo.getMinioUrl(), "/" + put.getBucketName() + "/" + baseFile.getFileName());
                    try {
                        inputStream.close();
                    } catch (Exception exception) {
                        log.error("导入时 关闭流文件失败", exception);
                        throw new BusinessException("导入时 关闭流文件失败");
                    }
                }
            }
            //入库
            DcLibrary parent = this.getById(parentId);
            DcLibrary dcLibrary = new DcLibrary();
            List<String> pathId = new ArrayList<>();
            if (!parent.getType().equals(DcLibraryTypeEnum.knowledge)) {
                pathId.addAll(parent.getPathId());
            }
            pathId.add(parent.getId());
            dcLibrary.setPathId(pathId);
            dcLibrary.setParentId(parentId);
            //文库
            dcLibrary.setKnowledgeId(parent.getKnowledgeId());
            // 默认排序
            DcLibrary one = this.getOne(Wrappers.<DcLibrary>lambdaQuery()
                    .select(DcLibrary::getOrderId)
                    .eq(DcLibrary::getParentId, dcLibrary.getParentId())
                    .orderByDesc(DcLibrary::getOrderId)
                    .last("limit 1"));
            int order = 0;
            if (Objects.nonNull(one)) {
                order = one.getOrderId() + 1;
            }
            dcLibrary.setOrderId(order);
            // 获取该文档的文件名称
            String originalName = FileOssUtils.getMultipartFileName(dcLibrary);
            //用户信息设置
            dcLibrary.setCreateBy(userDto.getRealName());
            dcLibrary.setCreateById(userDto.getId());
            dcLibrary.setCreateTime(LocalDateTime.now());
            dcLibrary.setUpdateBy(userDto.getRealName());
            dcLibrary.setUpdateTime(LocalDateTime.now());
            //新增目录或文档
            this.save(dcLibrary);
            // 将知识库名称当做文件的一级目录
            DcLibrary know = this.get(parent.getKnowledgeId());
            baseFile = ossTemplate.putContent(originalName, content, know.getName(), dcLibrary.getName());
            dcLibrary.setBucketName(baseFile.getBucketName());
            dcLibrary.setSize(baseFile.getSize());
            dcLibrary.setFilePath(baseFile.getFileName());
            UserDto currentUser = UserCurrentUtils.getCurrentUser();
            this.saveContent(currentUser.getId(), dcLibrary, dcLibrary.getId());
            //同步ES
            // 保存到文档es
            documentElasticService.save(currentUser, dcLibrary, content);
        } catch (Exception exception) {
            log.error("上传源文件执行错误", exception);
            if (exception.getCause() instanceof BusinessException) {
                errorMsg = exception.getCause().getMessage();
            } else {
                errorMsg = "未知错误";
            }
            errorLog = exception.getMessage();
        } finally {
            //删除整个目录中的所有文件
            FileUtil.del(unzipPath);
            //修改日志
            boolean notBlank = StrUtil.isNotBlank(errorMsg);
            sourceFileUdLogService.update(new UpdateWrapper<SourceFileUdLog>().lambda()
                    .set(SourceFileUdLog::getOperateStatus, notBlank ? SourceFileUdOperateStatusEnums.fail : SourceFileUdOperateStatusEnums.success)
                    .set(notBlank, SourceFileUdLog::getErrorLog, errorLog)
                    .set(notBlank, SourceFileUdLog::getErrorMessage, errorMsg)
                    .eq(SourceFileUdLog::getId, logId));
        }
    }

    @Override
    public BaseFile downSourceFile(String id) throws IOException {
        DcLibrary dcLibrary = this.getById(id);
        if (ObjectUtil.isNull(dcLibrary)) {
            throw new BusinessException("未找到此文件");
        }
        if (StrUtil.isEmpty(dcLibrary.getFilePath()) || StrUtil.isEmpty(dcLibrary.getBucketName())) {
            throw new BusinessException("文件内容为空无法下载");
        }
        String url = ossTemplate.fileLink(dcLibrary.getFilePath(), dcLibrary.getBucketName());
        byte[] bytes = HttpUtil.downloadBytes(url);
        String content = ObjectUtil.deserialize(bytes);
        String value = JSONObject.parseObject(content).getString("contentHtml");
        //获取附件地址 需要一起打包
        List<SourceFileDownTypePo> list = RichTextReplaceUrlUtil.getFile(value);
        List<SourceFileDownFilePo> sourceFileDownFilePos = new ArrayList<>(list.size());
        String separator = File.separator;
        String downPath = commonConfig.getDownPath();
        if (!list.isEmpty()) {
            list.forEach(e -> {
                SourceFileDownFilePo sourceFileDownFilePo = new SourceFileDownFilePo().setMinioUrl(e.getUrl());
                String filePath = id + "_" + SecureUtil.md5(e.getUrl());
                //获取后缀名
                String extName = FileUtil.extName(e.getUrl());
                try {
                    String saveFilePath = downPath + separator + DOWN_FILE_PATH + separator + filePath + "." + extName;
                    ZipUtil.downloadFile(ossUrl + e.getUrl(), saveFilePath);
                    sourceFileDownFilePo.setFielUrl(saveFilePath);
                    sourceFileDownFilePos.add(sourceFileDownFilePo);
                } catch (IOException ex) {
                    log.error("源文件下载失败", ex);
                }
            });
        }
        //打包文件与附件绑定关系
        ZipUtil.saveFile(content, downPath + CONTENT_FILE_NAME.concat(DcLibraryTypeEnum.document_html.value));
        //绑定关系
        ZipUtil.saveFile(JSONObject.toJSONString(sourceFileDownFilePos), downPath + separator + DOWN_ACCESSORY_JSON_NAME);
        //打包路径
        String zipFileName = IdGenerator.getIdStr() + ".zip";
        String zipFilePath = downPath + File.separator + zipFileName;
        ZipUtil.zipFile(downPath, zipFilePath, commonConfig.getZipPwd());
        //上传
        BufferedInputStream inputStream = FileUtil.getInputStream(zipFilePath);
        BaseFile baseFile = ossTemplate.putFile("jvs-public", "document-sourceFile", zipFileName, inputStream);
        inputStream.close();
        FileUtil.del(zipFilePath);
        return baseFile;
    }

    @Override
    public DcLibrary get(String id) {
        if (StringUtils.isBlank(id)) {
            throw new BusinessException("文档id为空");
        }
        DcLibrary dcLibrary = this.getById(id);
        if (Objects.isNull(dcLibrary)) {
            throw new BusinessException("文档不存在");
        }
        return dcLibrary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void locationOther(DcLocationOtherDto dcLocationOtherDto) {
        DcLibrary dcLibrary = this.getById(dcLocationOtherDto.getId());
        //判断是否改变了上级 如果没有变更就不要去修改下级数据 减少逻辑处理
        boolean superiorsIsUpdate = dcLibrary.getParentId().equals(dcLocationOtherDto.getParentId());
        //判断是否存在下级 如果不存在就表示放到此层级的最后一个位置
        String nextId = dcLocationOtherDto.getNextId();
        Integer orderId;
        if (StrUtil.isNotBlank(nextId)) {
            //获取当前插入的位置后面的所有数据
            DcLibrary nextDcLibrary = this.getById(nextId);
            orderId = nextDcLibrary.getOrderId();
            List<DcLibrary> list = this.list(new LambdaQueryWrapper<DcLibrary>()
                    .eq(DcLibrary::getParentId, dcLocationOtherDto.getParentId())
                    .le(DcLibrary::getOrderId, orderId)
                    .orderByDesc(DcLibrary::getOrderId));
            for (int i = 0; i < list.size(); i++) {
                int val = orderId - i - 1;
                this.update(new UpdateWrapper<DcLibrary>().lambda()
                        .eq(DcLibrary::getId, list.get(i).getId())
                        .set(DcLibrary::getOrderId, val));
            }
        } else {
            //获取排序
            DcLibrary one = this.getOne(Wrappers.<DcLibrary>lambdaQuery()
                    .select(DcLibrary::getOrderId)
                    .eq(DcLibrary::getParentId, dcLocationOtherDto.getParentId())
                    .orderByAsc(DcLibrary::getOrderId)
                    .last("limit 1"));
            orderId = ObjectUtil.isNull(one) ? 0 : one.getOrderId();
            orderId -= 1;
        }
        //改变排序值
        this.update(new UpdateWrapper<DcLibrary>().lambda()
                .eq(DcLibrary::getId, dcLocationOtherDto.getId())
                .set(DcLibrary::getOrderId, orderId)
                .set(DcLibrary::getParentId, dcLocationOtherDto.getParentId()));

        if (!superiorsIsUpdate) {
            //获取上级id的路径数据
            DcLibrary byId = this.getById(dcLocationOtherDto.getParentId());
            List<String> pathId = byId.getPathId();
            //添加本身
            pathId.add(byId.getId());
            //修改文库与权限数据
            dcAuthConfigService.moveUpdateAuthConfig(dcLocationOtherDto, pathId);
            //修改子集
            locationSubset(dcLocationOtherDto, pathId);
        }

    }

    /**
     * 移动时修改es数据
     *
     * @param dcLibrary 需要修改的对象
     */
    private void moveUpdateEsData(DcLibrary dcLibrary, String knowledgeName) {
        //修改es内容
        Document document = Document.create();
        document.put(Get.name(DocumentEsPo::getKnowledgeId), dcLibrary.getKnowledgeId());
        document.put(Get.name(DocumentEsPo::getPathId), String.join("/", dcLibrary.getPathId()));
        document.put(Get.name(DocumentEsPo::getKnowledgeName), knowledgeName);
        document.put(Get.name(DocumentEsPo::getParentId), dcLibrary.getParentId());
        documentElasticService.update(dcLibrary, document);
    }

    /**
     * 修改子集
     *
     * @param dcLocationOtherDto 入参
     * @param pathIds            需要修改的数据
     */
    private void locationSubset(DcLocationOtherDto dcLocationOtherDto, List<String> pathIds) {
        //获取本身与本身下面的所有数据并进行修改
        LambdaQueryWrapper<DcLibrary> queryWrapper = new QueryWrapper<DcLibrary>().lambda()
                .apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", dcLocationOtherDto.getId())
                .or()
                //添加本身
                .eq(DcLibrary::getId, dcLocationOtherDto.getId());
        List<DcLibrary> libraryList = this.list(queryWrapper);
        //修改所有下级的路径数据与
        libraryList.stream().peek(e -> {
            List<String> newPathId = new ArrayList<>();
            newPathId.addAll(pathIds);
            if (!e.getId().equals(dcLocationOtherDto.getId())) {
                log.info("本次对比的数据为:{},pathId为：{}", e.getId(), com.alibaba.fastjson2.JSONObject.toJSONString(e.getPathId()));
                List<String> pathId = e.getPathId();
                int index = pathId.indexOf(dcLocationOtherDto.getId());
                log.info("下标为:{}", index);
                List<String> list = pathId.subList(index, pathId.size());
                newPathId.addAll(list);
            }
            e.setPathId(newPathId).setKnowledgeId(dcLocationOtherDto.getKnowledgeId());
        }).collect(Collectors.toList());
        //修改数据
        this.updateBatchById(libraryList);
        //获取知识库信息
        DcLibrary library = this.getById(dcLocationOtherDto.getKnowledgeId());
        libraryList.forEach(e -> this.moveUpdateEsData(e, library.getName()));
    }


    @Override
    public Boolean checkRepetition(String id, String name, DcLibraryTypeEnum type, Boolean parent) {
        if (!parent) {
            DcLibrary library = this.getById(id);
            id = library.getParentId();
        }
        long count;
        //判断是否重复
        if (type.equals(DcLibraryTypeEnum.knowledge)) {
            count = this.count(new LambdaQueryWrapper<DcLibrary>()
                    .eq(DcLibrary::getType, DcLibraryTypeEnum.knowledge)
                    .eq(DcLibrary::getName, name));

        } else {
            LambdaQueryWrapper<DcLibrary> queryWrapper = new LambdaQueryWrapper<>();
            if (type.equals(DcLibraryTypeEnum.directory)) {
                queryWrapper.eq(DcLibrary::getType, DcLibraryTypeEnum.directory);
            } else {
                queryWrapper.notIn(DcLibrary::getType, Arrays.asList(DcLibraryTypeEnum.directory, DcLibraryTypeEnum.knowledge));
            }
            count = this.count(queryWrapper
                    .eq(DcLibrary::getName, name)
                    .eq(DcLibrary::getParentId, id));
        }
        return count > 0;
    }

    @Override
    public void fileTransition(String id) {
        DcLibrary dcLibrary = this.getById(id);
        log.info("转换时获取数据值为:{}", com.alibaba.fastjson2.JSONObject.toJSONString(dcLibrary));
        if (!Arrays.asList("doc", "xls").contains(dcLibrary.getNameSuffix())) {
            return;
        }
        String idStr = IdGenerator.getIdStr();
        OnlyOfficeFileTransitionVo onlyOfficeFileTransitionVo = new OnlyOfficeFileTransitionVo()
                .setAsync(Boolean.FALSE)
                .setFiletype(dcLibrary.getNameSuffix())
                .setKey(idStr);
        String url = ossTemplate.fileLink(dcLibrary.getFilePath(), dcLibrary.getBucketName());
        //新名称
        String newFileName = null;
        String newNameSuffix = null;
        switch (dcLibrary.getNameSuffix()) {
            case "doc":
                onlyOfficeFileTransitionVo
                        .setOutputtype("docx")
                        .setUrl(url)
                        .setTitle(idStr + "." + "docx");
                newFileName = dcLibrary.getName().replace("doc", "docx");
                newNameSuffix = "docx";
                break;
            case "xls":
                onlyOfficeFileTransitionVo
                        .setOutputtype("xlsx")
                        .setUrl(url)
                        .setTitle(idStr + "." + "xlsx");
                newFileName = dcLibrary.getName().replace("xls", "xlsx");
                newNameSuffix = "xlsx";
                break;
            default:
        }
        try {

            String transition = FileTransitionUtil.transition(onlyOfficeFileTransitionVo);
            InputStream inputStream = FileOssUtils.getInputStream(transition);
            BaseFile baseFile = ossTemplate.putFile(SpringContextUtil.getApplicationContextName(), SpringContextUtil.getApplicationContextName(), IdGenerator.getIdStr() + newFileName, inputStream);
            //修改数据
            this.update(new UpdateWrapper<DcLibrary>().lambda()
                    .set(DcLibrary::getName, newFileName)
                    .set(DcLibrary::getNameSuffix, newNameSuffix)
                    .set(DcLibrary::getFilePath, baseFile.getFileName())
                    .set(DcLibrary::getBucketName, baseFile.getBucketName())
                    .eq(DcLibrary::getId, id));
        } catch (Exception exception) {
            log.error("自动转换文件失败", exception);
        }
    }

    @Override
    public void clearAuth(String id) {

    }

    @Override
    public String downOffice(String id, String type) throws IOException {
        String htmlBody = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body>{}</body></html>";
        DcLibrary dcLibrary = this.getById(id);
        if (ObjectUtil.isNull(dcLibrary)) {
            throw new BusinessException("未找到此文件");
        }
        if (StrUtil.isEmpty(dcLibrary.getFilePath()) || StrUtil.isEmpty(dcLibrary.getBucketName())) {
            throw new BusinessException("文件内容为空无法下载");
        }
        //获取内容添加html 头部标签 并重新上传到minio
        String url = ossTemplate.fileLink(dcLibrary.getFilePath(), dcLibrary.getBucketName());
        byte[] bytes = HttpUtil.downloadBytes(url);
        String value = JSONObject.parseObject(ObjectUtil.deserialize(bytes)).getString("contentHtml");
        String prefix = ossUrl;
        //添加前缀
        if (!prefix.startsWith("http")) {
            prefix = "http://" + prefix;
        }
        value = RichTextReplaceUrlUtil.addImagePrefix(value, prefix);
        log.info("替换后的值为:{}", value);
        String content = StrUtil.format(htmlBody, value);
        String originalName = dcLibrary.getName() + ".html";
        bytes = content.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        BaseFile baseFile = ossTemplate.put(SpringContextUtil.getApplicationContextName(), SpringContextUtil.getApplicationContextName(), inputStream, originalName, Boolean.TRUE);
        String fileLink = ossTemplate.fileLink(baseFile.getFileName(), baseFile.getBucketName());
        inputStream.close();
        //请求onlyOffice
        OnlyOfficeFileTransitionVo onlyOfficeFileTransitionVo = new OnlyOfficeFileTransitionVo()
                .setAsync(Boolean.FALSE)
                .setFiletype("html")
                .setOutputtype(type)
                .setTitle(dcLibrary.getName() + "." + type)
                .setKey(IdGenerator.getIdStr())
                .setUrl(fileLink);
        return FileTransitionUtil.transition(onlyOfficeFileTransitionVo);
    }

    @Override
    public void getFile(String id) {
        //判断是否为分享
        DcLibrary byId = this.getById(id);
        if (byId == null) {
            throw new BusinessException("未找到此数据");
        }
        byte[] bytes = new byte[]{};
        if (StrUtil.isNotEmpty(byId.getBucketName()) && StrUtil.isNotEmpty(byId.getFilePath())) {
            String s = ossTemplate.fileLink(byId.getFilePath(), byId.getBucketName());
            bytes = HttpUtil.downloadBytes(s);
        }
        try {
            //判断是否为md文件
            if (byId.getType().equals(DcLibraryTypeEnum.md)) {
                String content = "";
                if (bytes != null && bytes.length > 0) {
                    content = bytes == null ? null : ObjectUtil.deserialize(bytes);
                    //获取内容
                    if (StrUtil.isNotEmpty(content)) {
                        JSONObject jsonObject = JSONObject.parseObject(content);
                        content = jsonObject.getString("content");
                    }
                }
                String fileName = byId.getName();
                if (!byId.getName().endsWith(".md")) {
                    fileName = fileName + ".md";
                }
                File file = FileUtil.file(fileName);
                //写入内容
                FileUtil.writeString(content, file, "utf-8");
                FileTreeUtil.fileOutput(fileName, FileUtil.readBytes(file));
                file.delete();
            } else {
                //判断是否需要添加后缀名
                String fileName = byId.getName();
                if (StrUtil.isNotBlank(byId.getNameSuffix()) && !byId.getName().endsWith("." + byId.getNameSuffix())) {
                    fileName = byId.getName() + "." + byId.getNameSuffix();
                }
                FileTreeUtil.fileOutput(fileName, bytes);
            }
        } catch (Exception e) {
            log.error("下载文件时获取文件错误", e);
            throw new BusinessException("获取文件错误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DcLibrary recycleDc(String id) {
        DcLibrary library = this.getById(id);
        if (ObjectUtil.isNotNull(library)) {
            LambdaQueryWrapper<DcLibrary> queryWrapper = new LambdaQueryWrapper<DcLibrary>().eq(DcLibrary::getId, id);
            if (library.getType().equals(DcLibraryTypeEnum.knowledge) || library.getType().equals(DcLibraryTypeEnum.directory)) {
                queryWrapper.or().apply("JSON_CONTAINS(path_id, CONCAT('\"',{0},'\"'))", id);
            }
            List<String> ids = this.list(queryWrapper).parallelStream().map(DcLibrary::getId).collect(Collectors.toList());
            DcLibraryRecycle dcLibraryRecycle = new DcLibraryRecycle()
                    .setDcId(id)
                    .setKnowledgeId(library.getType().equals(DcLibraryTypeEnum.knowledge) ? id : library.getKnowledgeId())
                    .setDcIdList(ids);
            dcLibraryRecycleService.save(dcLibraryRecycle);
            this.remove(queryWrapper);
            if (!ids.isEmpty()) {
                ids.forEach(e -> {
                    // 修改es数据
                    Document document = Document.create();
                    document.put(Get.name(DocumentEsPo::getIsDelete), Boolean.TRUE);
                    documentElasticService.update(new DcLibrary().setId(e).setTenantId(TenantContextHolder.getTenantId()), document);
                });
            }
        }
        return library;
    }

    @Override
    public List<String> getOwner() {
        return this.listObjs(new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getId).eq(DcLibrary::getPossessor, UserCurrentUtils.getUserId()).eq(DcLibrary::getType, DcLibraryTypeEnum.knowledge), Object::toString);
    }


    @Override
    @SneakyThrows
    public DcLibrary fileToSave(String id, BaseFile baseFile, String fileName, UserDto currentUser) {
        log.info("上传数据入库，入库文件信息:{}", JSONUtil.toJsonStr(baseFile));
        String file = baseFile.getFileName();
        if (StrUtil.isBlank(fileName)) {
            fileName = baseFile.getOriginalName();
        }
        //获取知识库id
        DcLibrary byId = this.getById(id);
        //如果是文件或上传的文件 就获取上级目录id
        List<DcLibraryTypeEnum> dcLibraryTypeEnums = Arrays.asList(DcLibraryTypeEnum.directory, DcLibraryTypeEnum.document_upload);
        if (dcLibraryTypeEnums.contains(byId.getType())) {
            byId = this.getById(byId.getParentId());
        }
        DcLibrary dcLibrary = new DcLibrary();
        String knowledgeId = byId.getType().equals(DcLibraryTypeEnum.knowledge) ? byId.getId() : byId.getKnowledgeId();
        //获取后缀名
        String suffix = FileUtil.getSuffix(file);
        //md文件需要解析出来重新上传
        DcLibraryTypeEnum type = DcLibraryTypeEnum.suffixToEnum(suffix);
        if (type.equals(DcLibraryTypeEnum.md)) {
            String content = MarkDownUtil.getContent(new URL(ossTemplate.fileLink(baseFile.getFileName(), baseFile.getBucketName())).openStream());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", content);
            //上传文件
            baseFile = ossTemplate.putContent(baseFile.getOriginalName(), jsonObject.toJSONString(), byId.getName(), dcLibrary.getName());
            jsonObject.put("contentHtml", content);
            dcLibrary.setContent(jsonObject.toString());
        }
        if (type.equals(DcLibraryTypeEnum.xmind)) {
            String content = XmindUtil.getContent(new URL(ossTemplate.fileLink(baseFile.getFileName(), baseFile.getBucketName())).openStream());
            //上传文件
            baseFile = ossTemplate.putContent(baseFile.getOriginalName(), content, byId.getName(), dcLibrary.getName());
            dcLibrary.setContent(content);
        }
        dcLibrary.setParentId(id)
                .setType(type)
                .setKnowledgeId(knowledgeId)
                .setNameSuffix(suffix);
        //查询点击的当前元素
        DcLibrary parent = this.getOne(Wrappers.<DcLibrary>lambdaQuery()
                .select(DcLibrary::getOrderId, DcLibrary::getPathId, DcLibrary::getType)
                .eq(DcLibrary::getParentId, dcLibrary.getParentId())
                .orderByDesc(DcLibrary::getOrderId)
                .last("limit 1"));
        int orderId = ObjectUtil.isNull(parent) ? 0 : parent.getOrderId();
        dcLibrary.setFilePath(baseFile.getFileName())
                .setBucketName(baseFile.getBucketName())
                .setName(fileName)
                .setOrderId(orderId + 1)
                .setTenantId(TenantContextHolder.getTenantId());
        dcLibrary.setCreateById(currentUser.getId());
        dcLibrary.setCreateBy(currentUser.getRealName());
        dcLibrary.setSize(baseFile.getSize());
        //设置路径树id集合
        //获取上级的路径参数
        List<String> pathId = new ArrayList<>();
        //获取上级数据
        DcLibrary pare = this.getById(id);
        if (!pare.getType().equals(DcLibraryTypeEnum.knowledge)) {
            pathId.addAll(pare.getPathId());
        }
        pathId.add(pare.getId());
        dcLibrary.setPathId(pathId);
        this.save(dcLibrary);
        //入队列
        DocumentMqEvent documentMqEvent = new DocumentMqEvent()
                .setDocumentId(dcLibrary.getId())
                .setContent(dcLibrary.getContent())
                .setTenantId(TenantContextHolder.getTenantId())
                .setUserDto(currentUser);
        documentConsumer.sendFileToEs(documentMqEvent);
        return dcLibrary;
    }

    @Override
    public DcLibrary fileToSave(String id, BaseFile baseFile, String fileName) {
        return this.fileToSave(id, baseFile, fileName, UserCurrentUtils.getCurrentUser());
    }


    @Override
    public void removeDc(DcLibrary dcLibrary) {
        if (ObjectUtil.isEmpty(dcLibrary)) {
            return;
        }
        DcLibraryTypeEnum type = dcLibrary.getType();
        if (DcLibraryTypeEnum.knowledge.equals(type)) {
            dcAuthConfigService.remove(new LambdaQueryWrapper<DcAuthConfig>().eq(DcAuthConfig::getDcId, dcLibrary.getId()));
        }
        List<DcLibrary> list = baseMapper.recycleChildById(dcLibrary.getId(), type.getValue());
        //获取需要删除的id
        List<String> ids = list.parallelStream().map(DcLibrary::getId).collect(Collectors.toList());
        baseMapper.deleteRecycle(dcLibrary.getId(), type.getValue());
        dcLibraryRecycleService.remove(new LambdaQueryWrapper<DcLibraryRecycle>().eq(DcLibraryRecycle::getDcId, dcLibrary.getId()));
        if (!list.isEmpty()) {
            // 删除对应的es数据
            String tenantId = TenantContextHolder.getTenantId();
            documentElasticService.deleteDocument(tenantId, ids);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDc(String id) {
        //获取需要删除的所有数据
        DcLibraryRecycle recycleServiceOne = dcLibraryRecycleService.getOne(new LambdaQueryWrapper<DcLibraryRecycle>().eq(DcLibraryRecycle::getDcId, id));
        recycleServiceOne.getDcIdList().forEach(e -> {
            DcLibrary library = baseMapper.getDeleteById(e);
            this.removeDc(library);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DcLibrary add(UserDto userDto, DcLibraryAddReqVo reqVo) {
        String knowledgeId = reqVo.getId();
        DcLibrary dcLibrary = new DcLibrary();
        BeanUtil.copyProperties(reqVo, dcLibrary);
        dcLibrary.setId(null);
        //设置路径目录id
        dcLibrary.setPathId(new ArrayList<>());
        boolean blank = StringUtils.isBlank(knowledgeId);
        if (!blank) {
            //设置上级
            String parentId = reqVo.getParentId();
            DcLibrary parent = this.getById(parentId);
            List<String> pathId = new ArrayList<>();
            if (!parent.getType().equals(DcLibraryTypeEnum.knowledge)) {
                pathId.addAll(parent.getPathId());
            }
            pathId.add(parent.getId());
            dcLibrary.setPathId(pathId);
        }
        //新增 知识库（任何人都可以创建知识库）
        if (blank) {
            //只需要名称和类型
            dcLibrary.setType(DcLibraryTypeEnum.knowledge);
            //设置所有者
            dcLibrary.setPossessor(userDto.getId());
            this.save(dcLibrary);
            //保存权限
            dcAuthConfigService.addOrUpdateAuthConfig(dcLibrary, new ArrayList<>(), true);
            return dcLibrary;
        }
        //查询点击的当前元素
        DcLibrary knowledge = this.getById(knowledgeId);
        if (Objects.isNull(knowledge)) {
            throw new BusinessException("该目录不存在");
        }
        //todo 权限认证
        DcLibraryTypeEnum type = knowledge.getType();
        switch (type) {
            case knowledge:
                // 只能新增目录
                // 子集 目录
                dcLibrary.setType(DcLibraryTypeEnum.directory);
            case directory:
                // 为空默认给目录
                DcLibraryTypeEnum fileType = Optional.ofNullable(reqVo.getFileType()).orElse(DcLibraryTypeEnum.directory);
                // 如果为传递的知识库变为目录
                if (DcLibraryTypeEnum.knowledge.equals(fileType)) {
                    fileType = DcLibraryTypeEnum.directory;
                }
                dcLibrary.setType(fileType);
                break;
            default:
                log.warn("未知的知识库类型");
                break;
        }
        //设置知识库id
        dcLibrary.setKnowledgeId(Optional.ofNullable(this.getKnowledgeByChildren(knowledgeId)).orElseThrow(() -> new BusinessException("该" + dcLibrary.getName() + "对应的知识库不存在")).getId());
        // 默认排序
        DcLibrary one = this.getOne(Wrappers.<DcLibrary>lambdaQuery()
                .select(DcLibrary::getOrderId)
                .eq(DcLibrary::getParentId, dcLibrary.getParentId())
                .orderByDesc(DcLibrary::getOrderId)
                .last("limit 1"));
        int order = 0;
        if (Objects.nonNull(one)) {
            order = one.getOrderId() + 1;
        }
        dcLibrary.setOrderId(order);
        //新增目录或文档
        this.save(dcLibrary);
        return dcLibrary;
    }


    @Override
    public List<String> getTreeName(String id) {
        DcLibrary byId = this.getById(id);
        if (byId.getType().equals(DcLibraryTypeEnum.knowledge)) {
            return Arrays.asList(byId.getName());
        }
        List<DcLibrary> list = this.list(new LambdaQueryWrapper<DcLibrary>().eq(DcLibrary::getId, id).or().eq(DcLibrary::getKnowledgeId, id));
        List<String> treeName = treeName(list, id, new ArrayList<>());
        Collections.reverse(treeName);
        return treeName;
    }

    private List<String> treeName(List<DcLibrary> list, String nodeId, List<String> treeName) {
        DcLibrary dcLibrary = list.parallelStream().filter(e -> e.getId().equals(nodeId)).findFirst().orElseThrow(() -> new BusinessException("数据中断!"));
        treeName.add(dcLibrary.getName());
        if (dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
            return treeName;
        }
        return treeName(list, dcLibrary.getParentId(), treeName);
    }

    @Override
    public List<DcLibrary> currentLevel() {
        //获取用户可以查看的数据
        DocumentAuthDto documentAuth = AuthSystemTool.getDocumentAuth();
        if (documentAuth.getIds().isEmpty()) {
            return new ArrayList<>();
        }
        List<DcLibrary> list = this.list(new LambdaQueryWrapper<DcLibrary>().in(DcLibrary::getId, documentAuth.getIds()).orderByDesc(DcLibrary::getOrderId));
        if (list.isEmpty()) {
            return list;
        }
        //设置权限
        Map<String, List<IdentifyingAuthDto>> map = documentAuth.getIdentifyingRoleMap();
        return list.stream().peek(e -> e.setDcIdentifying(map.get(e.getId()))).collect(Collectors.toList());
    }


    @Override
    public DcLibrary getKnowledgeByChildren(String id) {
        DcLibrary library = this.getById(id);
        if (library == null) {
            return null;
        } else if (library.getType() == DcLibraryTypeEnum.knowledge) {
            return library;
        } else {
            return getById(library.getKnowledgeId());
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public DcLibrary put(UserDto userDto, DcLibraryEditReqVo dto) {
        //判断是不是重命名
        DcLibrary library = this.getById(dto.getId());
        if (library == null) {
            throw new BusinessException("数据不存在");
        }
        BeanUtil.copyProperties(dto, library, CopyOptions.create().ignoreNullValue());
        dcAuthConfigService.addOrUpdateAuthConfig(library, dto.getAddAuthConfigDto(), false);
        // 转移项目
        if (StringUtils.isNotBlank(dto.getTransfer())) {
            //查询用户名称
            UserDto data = authUserServiceApi.getById(dto.getTransfer()).getData();
            //修改权限
            dcAuthConfigService.update(new UpdateWrapper<DcAuthConfig>()
                    .lambda()
                    .set(DcAuthConfig::getUserId, dto.getTransfer())
                    .set(DcAuthConfig::getName, data.getRealName())
                    .set(DcAuthConfig::getHeadImg, data.getHeadImg())
                    .eq(DcAuthConfig::getUserId, library.getPossessor())
                    .eq(DcAuthConfig::getType, DcLibraryTypeEnum.knowledge)
                    .eq(DcAuthConfig::getDcId, dto.getId()));
            library.setPossessor(dto.getTransfer());
            //修改消息的配置
            messageConfigService.update(new UpdateWrapper<MessageConfig>().lambda()
                    .set(MessageConfig::getUserId, dto.getTransfer())
                    .eq(MessageConfig::getBusinessId, dto.getId())
                    .eq(MessageConfig::getType, DcLibraryTypeEnum.knowledge)
                    .eq(MessageConfig::getUserId, library.getPossessor()));
        }
        //用户先拖动更新路径 再修改同一个文件的名称 前端的pid 没有更新 会修改为原来的路径 这里需要排除掉
        library.setParentId(null)
                .setPath(null)
                .setOrderId(null);
        this.updateById(library);
        return library;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Page<DcAuthConfig> queryUser(Page<DcAuthConfig> page, String id) {
        Page<DcAuthConfig> configPage = dcAuthConfigService.page(page, new LambdaQueryWrapper<DcAuthConfig>().eq(DcAuthConfig::getDcId, id));
        List<DcAuthConfig> collect = configPage.getRecords().stream().distinct().collect(Collectors.toList());

        configPage.setRecords(collect);
        return configPage;
    }


    @Override
    public Page<DcLibrary> queryKnowledge(Page<DcLibrary> page, DcLibrary dcLibrary) {
        LibraryAuthDto libraryAuth = AuthSystemTool.getLibraryAuth();
        List<String> allIds = libraryAuth.getAllIds();
        Map<String, List<IdentifyingAuthDto>> authSign = libraryAuth.getAuthSign();
        if (!allIds.isEmpty()) {
            // 获取可查看的知识库信息(作为成员的知识库 + 全租户下完全开放的知识库 + 当前租户下开放的知识库)
            LambdaQueryWrapper<DcLibrary> queryKnowledge = Wrappers.<DcLibrary>lambdaQuery()
                    .in(DcLibrary::getId, allIds)
                    .eq(ObjectUtil.isNotEmpty(dcLibrary.getShareRole()), DcLibrary::getShareRole, dcLibrary.getShareRole())
                    .like(StrUtil.isNotBlank(dcLibrary.getName()), DcLibrary::getName, dcLibrary.getName())
                    .orderByAsc(DcLibrary::getOrderId).orderByDesc(DcLibrary::getCreateTime);
            this.page(page, queryKnowledge);
            page.getRecords().stream().peek(e -> e.setDcIdentifying(authSign.getOrDefault(e.getId(), new ArrayList<>()))).collect(Collectors.toList());
            toppingSort(page);
        }
        return page;
    }

    /**
     * 置顶排序
     */
    private void toppingSort(Page<DcLibrary> page) {
        //排序
        List<String> list = dcToppingService.list(new LambdaQueryWrapper<DcTopping>().eq(DcTopping::getUserId, UserCurrentUtils.getUserId()).orderByDesc(DcTopping::getCreateTime)).parallelStream().map(DcTopping::getDcId).collect(Collectors.toList());
        List<DcLibrary> dcLibraries = page.getRecords().parallelStream().peek(e -> {
                    boolean contains = list.contains(e.getId());
                    e.setTopping(contains ? list.indexOf(e.getId()) : list.size())
                            .setIsTopping(contains);
                }).sorted(Comparator.comparingInt(DcLibrary::getTopping))
                .collect(Collectors.toList());
        page.setRecords(dcLibraries);
    }

    @Override
    public Page<DcLibrary> queryOwnerKnowledge(Page<DcLibrary> page, DcLibrary dcLibrary) {
        // 查询自己拥有的知识库id
        LibraryAuthDto libraryAuth = AuthSystemTool.getLibraryAuth();
        Map<String, List<IdentifyingAuthDto>> authSign = libraryAuth.getAuthSign();
        // 查询知识库信息
        if (CollectionUtils.isNotEmpty(libraryAuth.getIds())) {
            LambdaQueryWrapper<DcLibrary> queryKnowledge = Wrappers.<DcLibrary>lambdaQuery()
                    .in(DcLibrary::getId, libraryAuth.getIds())
                    .eq(StrUtil.isNotBlank(dcLibrary.getName()), DcLibrary::getName, dcLibrary.getName())
                    .orderByAsc(DcLibrary::getOrderId)
                    .orderByDesc(DcLibrary::getCreateTime);

            this.page(page, queryKnowledge);
            page.getRecords().stream().peek(e -> e.setDcIdentifying(authSign.getOrDefault(e.getId(), new ArrayList<>()))).collect(Collectors.toList());
            toppingSort(page);
        }
        return page;
    }


    @Override
    public Set<String> getAllChildDocumentId(String id) {
        DcLibrary dcLibrary = this.get(id);
        List<DcLibrary> child = this.getChild(dcLibrary, false, null);
        dcLibrary.setChildren(child);
        List<DcLibrary> dcLibraries = TreeUtils.tree2List(dcLibrary, DcLibrary::getChildren);
        return dcLibraries.stream()
                // 排除自身
                .filter(e -> !id.equals(e.getId()))
                .filter(e -> e.getType().getIsDoc())
                .map(DcLibrary::getId)
                .collect(Collectors.toSet());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveContent(String userId, DcLibrary dcLibrary, String documentId) {
        if (dcLibrary == null || dcLibrary.getType() == DcLibraryTypeEnum.directory || dcLibrary.getType() == DcLibraryTypeEnum.knowledge) {
            throw new BusinessException("文档不存在，参数错误");
        }
        updateById(dcLibrary);
    }


    @Override
    public PreviewDocumentResVo preview(String id, UserDto userDto) {
        DcLibrary library = getById(id);
        if (library == null) {
            throw new BusinessException("文档不存在");
        }
        return preview(library, userDto);
    }

    @Override
    public PreviewDocumentResVo preview(DcLibrary library, UserDto userDto) {
        String userId = Optional.ofNullable(userDto).orElse(new UserDto()).getId();

        PreviewDocumentResVo resVo = new PreviewDocumentResVo();
        resVo.setCreateTime(library.getCreateTime())
                .setAuthor(library.getCreateBy())
                .setOtherJson(library.getOtherJson())
                .setUpdateTime(library.getUpdateTime());
        //内容
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(library.getFilePath())) {
            String objectUrl = ossTemplate.fileLink(library.getFilePath(), library.getBucketName());
            log.info("获取的地址为：{}", objectUrl);
            if (library.getType().equals(DcLibraryTypeEnum.document_unrecognized)) {
                resVo.setContent(objectUrl);
            } else {
                byte[] bytes = HttpUtil.downloadBytes(objectUrl);
                resVo.setContent(bytes == null ? null : ObjectUtil.deserialize(bytes));
            }
        }
        if (StringUtils.isNotBlank(userId)) {
            // 是否收藏
            long count = dcLibraryCollectService.count(Wrappers.<DcLibraryCollect>lambdaQuery()
                    .eq(DcLibraryCollect::getUserId, userId)
                    .eq(DcLibraryCollect::getDocId, library.getId()));
            resVo.setCollected(count != 0);
        }

        // 日志入库ES
        return resVo;
    }

    @Override
    public DcLibrary getAllId(String id) {
        return baseMapper.getAllById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retrieve(DcLibrary dcLibrary) {
        log.info("恢复的数据为:{}", JSONObject.toJSONString(dcLibrary));
        //需要判断 父级和知识库是否已经恢复
        if (!dcLibrary.getType().equals(DcLibraryTypeEnum.knowledge)) {
            List<String> idList = Arrays.asList(dcLibrary.getKnowledgeId(), dcLibrary.getParentId()).stream().distinct().collect(Collectors.toList());
            List<DcLibrary> list = this.listByIds(idList);
            //比对数量是否相等不想等 就表示父级或者知识库没有恢复
            if (idList.size() != list.size()) {
                throw new BusinessException("无法恢复，请先恢复上级目录与知识库!");
            }
        }
        DcLibraryRecycle recycleServiceOne = dcLibraryRecycleService.getOne(new LambdaQueryWrapper<DcLibraryRecycle>().eq(DcLibraryRecycle::getDcId, dcLibrary.getId()));
        recycleServiceOne.getDcIdList().forEach(e -> {
            log.info("当前id：{}", e);
            DcLibrary byId = baseMapper.getDeleteById(e);
            //数据库
            baseMapper.retrieve(e);
            // 修改es数据
            Document document = Document.create();
            document.put(Get.name(DocumentEsPo::getIsDelete), Boolean.FALSE);
            documentElasticService.update(byId, document);
        });
        dcLibraryRecycleService.remove(new LambdaQueryWrapper<DcLibraryRecycle>().eq(DcLibraryRecycle::getDcId, dcLibrary.getId()));
    }


    @Override
    public void recycle(Page<DcLibrary> page) {
        List<String> ids = baseMapper.getCreateUser(UserCurrentUtils.getUserId(), TenantContextHolder.getTenantId());
        Page<DcLibraryRecycle> dcLibraryRecyclePage = new Page<>();
        dcLibraryRecyclePage.setCurrent(page.getCurrent());
        dcLibraryRecyclePage.setSize(page.getSize());
        if (ids.isEmpty()) {
            return;
        }
        dcLibraryRecyclePage = dcLibraryRecycleService.page(dcLibraryRecyclePage, new LambdaQueryWrapper<DcLibraryRecycle>()
                .in(DcLibraryRecycle::getKnowledgeId, ids)
                .orderByDesc(DcLibraryRecycle::getCreateTime));
        List<String> list = dcLibraryRecyclePage.getRecords().stream().map(DcLibraryRecycle::getDcId).collect(Collectors.toList());
        List<DcLibrary> collect = list.stream().map(e -> baseMapper.getDeleteById(e)).collect(Collectors.toList());
        page.setRecords(collect)
                .setTotal(dcLibraryRecyclePage.getTotal());
    }

    /**
     * 获取所有子文档/子目录
     *
     * @param library          知识库/目录
     * @param returnAllColumns 查询所有字段
     * @return 子节点集合
     */
    private List<DcLibrary> getChild(DcLibrary library, boolean returnAllColumns, String search) {
        String id = library.getId();
        DcLibraryTypeEnum type = library.getType();
        // 获取知识库id
        String knowledgeId;
        if (DcLibraryTypeEnum.knowledge.equals(type)) {
            knowledgeId = id;
        } else {
            knowledgeId = library.getKnowledgeId();
        }
        LambdaQueryWrapper<DcLibrary> wrapper = Wrappers.lambdaQuery();
        if (!returnAllColumns) {
            // 只查询部分字段
            wrapper.select(DcLibrary::getId, DcLibrary::getUrlAddress, DcLibrary::getParentId, DcLibrary::getType, DcLibrary::getOrderId);
        }
        // 获取知识库, 和所有的子文档/子目录
        List<DcLibrary> all = this.list(wrapper
                .eq(DcLibrary::getId, knowledgeId)
                .or()
                .eq(DcLibrary::getKnowledgeId, knowledgeId)
        );

        if (StrUtil.isNotBlank(search)) {
            all = this.filterBySearch(search, all);
        }

        if (ObjectUtils.isEmpty(all)) {
            return Collections.emptyList();
        }
        // 转树形结构
        DcLibrary root = TreeUtils.list2Tree(all, id, DcLibrary::getId, DcLibrary::getParentId, DcLibrary::setChildren);
        if (Objects.isNull(root)) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(root.getChildren()).orElse(Collections.emptyList());
    }

    @Override
    public void resetSort(ResetSortDto resetSortDto) {
        List<DcLibrary> list;
        if ("name".equals(resetSortDto.getSortName())) {
            list = baseMapper.resetSort(resetSortDto.getId(), resetSortDto.getIsSortDesc());
        } else {
            LambdaQueryWrapper<DcLibrary> eq = new LambdaQueryWrapper<DcLibrary>()
                    .eq(DcLibrary::getKnowledgeId, resetSortDto.getId())
                    .orderBy("createTime".equals(resetSortDto.getSortName()), resetSortDto.getIsSortDesc(), DcLibrary::getCreateTime);
            list = this.list(eq);

        }
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setOrderId(i + 1);
            }
        }
        this.updateBatchById(list);
        //修改排序字段
        this.update(new UpdateWrapper<DcLibrary>().lambda().eq(DcLibrary::getId, resetSortDto.getId())
                .set(DcLibrary::getSortFieldName, resetSortDto.getSortName())
                .set(DcLibrary::getSortDescIs, resetSortDto.getIsSortDesc()));
    }

    /**
     * 过滤
     *
     * @param search
     * @param all
     * @return
     */
    private List<DcLibrary> filterBySearch(String search, List<DcLibrary> all) {
        if (StrUtil.isNotBlank(search)) {
            List<DcLibrary> collect = all.stream().filter(e -> ObjectUtil.equals(e.getType(), DcLibraryTypeEnum.knowledge)
                    || ObjectUtil.equals(e.getType(), DcLibraryTypeEnum.directory)
                    || StrUtil.contains(e.getName(), search)
            ).collect(Collectors.toList());

            //判断目录下是否有文件 没有文件则删除
            Map<String, DcLibrary> dataMap = collect.stream().collect(Collectors.toMap(DcLibrary::getId, Function.identity()));

            Map<String, Long> pathCountMap = collect.stream().filter(e -> !ObjectUtil.equals(e.getType(), DcLibraryTypeEnum.knowledge)
                            && !ObjectUtil.equals(e.getType(), DcLibraryTypeEnum.directory))
                    .peek(e -> this.buildPath(e, e.getParentId(), dataMap))
                    .collect(Collectors.groupingBy(DcLibrary::getPath, Collectors.counting()));
            List<String> pathList = pathCountMap.keySet().stream().filter(e -> pathCountMap.getOrDefault(e, 0L) > 0).collect(Collectors.toList());

            collect.removeIf(e -> {
                if (ObjectUtil.equals(e.getType(), DcLibraryTypeEnum.directory)) {
                    this.buildPath(e, e.getParentId(), dataMap);
                    boolean b = pathList.stream().anyMatch(o -> o.contains(e.getPath()));
                    return !b;
                }
                return Boolean.FALSE;
            });
            all = collect;
        }
        return all;
    }

    /**
     * 创建路径
     *
     * @param parentId
     * @param dataMap
     */
    private void buildPath(DcLibrary dc, String parentId, Map<String, DcLibrary> dataMap) {
        if (StrUtil.equals(parentId, "0")) {
            return;
        }
        DcLibrary dcLibrary = dataMap.get(parentId);
        if (StrUtil.isBlank(dc.getPath())) {
            dc.setPath(dc.getName());
        }
        String path = StrUtil.concat(true, dcLibrary.getName(), "/", dc.getPath());
        dc.setPath(path);
        this.buildPath(dc, dcLibrary.getParentId(), dataMap);
    }
}
