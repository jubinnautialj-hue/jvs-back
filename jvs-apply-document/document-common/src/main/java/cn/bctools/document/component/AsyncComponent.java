package cn.bctools.document.component;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.R;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.constant.Constant;
import cn.bctools.document.constant.IndexConstant;
import cn.bctools.document.dto.IdentifyingAuthDto;
import cn.bctools.document.dto.UpgradeDto;
import cn.bctools.document.entity.DcAuthConfig;
import cn.bctools.document.entity.DcIdentifying;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcLibraryRecycle;
import cn.bctools.document.entity.enums.DataAuthTypeEnum;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.bctools.document.listener.es.DocumentMqEvent;
import cn.bctools.document.mapper.DcLibraryMapper;
import cn.bctools.document.message.inside.InsideMessagePushPush;
import cn.bctools.document.office.OfficeContentUtil;
import cn.bctools.document.po.DocumentEsPo;
import cn.bctools.document.po.UnzipPo;
import cn.bctools.document.receiver.dto.DocumentMqDto;
import cn.bctools.document.service.*;
import cn.bctools.document.util.ZipFileUtil;
import cn.bctools.document.vo.req.DcLibraryAddReqVo;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author xiaohui
 */
@Slf4j
@Service
@AllArgsConstructor
public class AsyncComponent {
    private final OssTemplate ossTemplate;
    private final DocumentElasticService documentElasticService;
    private final DcLibraryMapper dcLibraryMapper;
    private final InsideNotificationApi insideNotificationApi;
    private final DcIdentifyingService dciIdentifyingService;
    private final AuthUserServiceApi authUserServiceApi;
    private final ElasticsearchRestTemplate esTemplate;
    private final DcAuthConfigService dcAuthConfigService;
    private final DcLibraryRecycleService dcLibraryRecycleService;


    /**
     * 桶名称
     */
    private static final String BUCKET_NAME = "document-mgr";
    /**
     * es同步成功后的消息通知内容
     */
    private static final String MESSAGE_CONTENT = "文件名称:{},{}{},消耗时间为:{}ms";
    /**
     * 英文
     */
    private static final String MESSAGE_CONTENT_EN = "file name:{},{}{},consumption of time:{}ms";

    /**
     * 异步保存文件内容
     *
     * @param documentMqDto 文档信息
     */
    public void asyncDcLibraryFileToSave(DocumentMqEvent documentMqDto) {
        log.info("进入文件处理");
        if (ObjectUtil.isNotEmpty(documentMqDto.getLocale())) {
            LocaleContextHolder.setLocale(documentMqDto.getLocale());
        }
        TimeInterval timer = new TimeInterval();
        DcLibrary dcLibrary = dcLibraryMapper.selectById(documentMqDto.getDocumentId());
        String status = "成功";
        //上传文件异步处理
        try {
            String content;
            //判断是否已经存在内容了 例如上传md时需要先解析内容在入库 这个时候 已经解析了 就不需要二次解析了
            if (StrUtil.isNotBlank(documentMqDto.getContent())) {
                content = documentMqDto.getContent();
            } else {
                String url = ossTemplate.fileLink(dcLibrary.getFilePath(), dcLibrary.getBucketName());
                content = OfficeContentUtil.getOfficeContent(dcLibrary.getNameSuffix(), url, Boolean.FALSE);
            }
            log.info("文档处理完成,开始同步到es");
            documentElasticService.save(documentMqDto.getUserDto(), dcLibrary, content);
        } catch (Exception e) {
            log.info("文档异步入es错误", e);
            status = "失败";
        }
        //消息通知
//        this.pushMessage(dcLibrary.getName(), "同步搜索引擎", status, documentMqDto.getUserDto(), timer.intervalMs());
    }


    /**
     * 消息发送
     *
     * @param fileName 文档名称
     * @param userDto  此操作发起人信息
     * @param status   执行状态文本
     * @param typeStr  类型文本
     * @param time     执行时间
     */
    private void pushMessage(String fileName, String typeStr, String status, UserDto userDto, long time) {
        String msg = SpringContextUtil.msg(typeStr);
        Locale locale = LocaleContextHolder.getLocale();
        String messageContent = MESSAGE_CONTENT;
        if (locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
            messageContent = MESSAGE_CONTENT_EN;
        }
        InsideMessagePushPush insideMessagePushPush = SpringContextUtil.getBean(InsideMessagePushPush.class);
        insideMessagePushPush.textPushMessage(StrUtil.format(messageContent, fileName, msg, status, time), userDto);
    }

    /**
     * 2.1.7升级到2.1.8数据适配
     *
     * @param jsonObject 历史数据的json
     * @param userDto    当前操作的用户信息
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void upgrade(JSONObject jsonObject, UserDto userDto) {
        Boolean success = Boolean.TRUE;
        DcLibraryService dcLibraryService = SpringContextUtil.getBean(DcLibraryService.class);
        //清空回收站
        String messageTxt = "数据升级到2.1.8";
        try {
            JSONArray records = jsonObject.getJSONArray("RECORDS");
            List<UpgradeDto> upgradeDto = records.toJavaList(UpgradeDto.class);
            //删除es中的所有内容
            esTemplate.indexOps(IndexConstant.INDEX_DOCUMENT_BASE_INFO).delete();
            //2.1.7版本的数据 如果角色id为1 就表示是所有者 只需要把所有者数据适配就可以了 原有的所有权限不管
            //先获取所有权限
            //如果是知识库,创建所属人
            List<DcIdentifying> identifyingList = dciIdentifyingService.basisTypeToPossessor(null, Boolean.TRUE);
            List<IdentifyingAuthDto> authSign = BeanUtil.copyToList(identifyingList, IdentifyingAuthDto.class);
            //设置默认值
            authSign.stream().peek(e -> e.setIsSelect(true).setSelect(true)).collect(Collectors.toList());
            //忽略租户
            TenantContextHolder.clear();
            List<String> ids = upgradeDto.stream().map(UpgradeDto::getDcId).collect(Collectors.toList());
            //判断文库中是否存在
            ids = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>().select(DcLibrary::getId).in(DcLibrary::getId, ids))
                    .stream().map(DcLibrary::getId).collect(Collectors.toList());

            List<String> finalIds = ids;
            List<DcAuthConfig> list = upgradeDto.stream()
                    .filter(e -> "1".equals(e.getRoleId()))
                    .filter(e -> finalIds.contains(e.getDcId()))
                    .map(e -> {
                        DcAuthConfig dcAuthConfig = null;
                        R<UserDto> byId = new R<>();
                        try {
                            byId = authUserServiceApi.getById(e.getUserId());
                        } catch (Exception exception) {
                            log.info("没有找到此用户的数据");
                            byId.setCode(-1);
                        }
                        if (byId.getCode() == 0) {
                            UserDto user = byId.getData();
                            if (user != null) {
                                dcAuthConfig = new DcAuthConfig()
                                        .setDataAuthType(DataAuthTypeEnum.user)
                                        .setPathId(new ArrayList<>())
                                        .setAuthSign(authSign)
                                        .setHeadImg(user.getHeadImg())
                                        .setName(user.getRealName())
                                        .setDcId(e.getDcId())
                                        .setKnowledgeId(e.getDcId())
                                        .setUserId(e.getUserId());
                            }
                        }
                        return dcAuthConfig;
                    })
                    .filter(ObjectUtil::isNotNull)
                    .collect(Collectors.toList());
            //添加数据的链路并同步到es中
            //历史脏数据的判断
            Predicate<DcLibrary> predicate = e -> (e.getPathId() == null || e.getPathId().isEmpty()) && !e.getType().equals(DcLibraryTypeEnum.knowledge);
            //通过上面的数据 修改文库数据 单个文库操作完成后在操作其他数据防止数据过多 内存溢出
            list.forEach(e -> {
                //每次都需要清理租户id 然后通过 知识库的租户id 设置当前租户id
                TenantContextHolder.clear();
                DcLibrary byId = dcLibraryService.getById(e.getDcId());
                //设置所有者id
                dcLibraryService.update(new UpdateWrapper<DcLibrary>().lambda().eq(DcLibrary::getId, e.getDcId())
                        .set(DcLibrary::getPossessor, e.getUserId()));
                TenantContextHolder.setTenantId(byId.getTenantId());
                //获取当前知识库的所有数据
                List<DcLibrary> libraries = dcLibraryService.list(new LambdaQueryWrapper<DcLibrary>()
                        .eq(DcLibrary::getKnowledgeId, e.getDcId()).or().eq(DcLibrary::getId, e.getDcId()));
                List<String> pathId = new ArrayList<String>();
                pathId.add(e.getDcId());
                knowledgeUpdate(libraries, e.getDcId(), pathId);
                //保存权限数据
                dcAuthConfigService.save(e);
                //过滤脏数据
                List<DcLibrary> updateList = libraries.stream().filter(v -> !predicate.test(v)).collect(Collectors.toList());
                dcLibraryService.updateBatchById(updateList);
                //设置文库的
                //es同步
                List<DcLibraryTypeEnum> typeEnums = Arrays.asList(DcLibraryTypeEnum.document_html, DcLibraryTypeEnum.md, DcLibraryTypeEnum.xmind, DcLibraryTypeEnum.document_xlsx);
                List<DocumentEsPo> esPoList = updateList.stream().filter(v -> DcLibraryTypeEnum.isFile(v.getType()))
                        .map(v -> {
                            String content = "";
                            if (typeEnums.contains(v.getType())) {
                                if (!StringUtils.isBlank(v.getFilePath())) {
                                    String url = ossTemplate.fileLink(v.getFilePath(), v.getBucketName());
                                    try {
                                        byte[] bytes = HttpUtil.downloadBytes(url);
                                        content = bytes == null ? null : ObjectUtil.deserialize(bytes);
                                    } catch (Exception exception) {
                                        log.info("通过文档地址获取文件错误{},文件路径:{},桶名称{}", exception.getMessage(), v.getFilePath(), v.getBucketName());
                                        content = null;
                                    }
                                }
                            }
                            //是否为office数据
                            if (DcLibraryTypeEnum.isOffice(v.getType())) {
                                log.info("开始获取office内容:{},", JSONUtil.toJsonStr(v));
                                try {
                                    log.info("------------------------当前的数据为,{},文件地址{},桶名称{}", v.getId(), v.getFilePath(), v.getBucketName());
                                    content = OfficeContentUtil.getOfficeContent(FileUtil.getSuffix(v.getFilePath()), new URL(ossTemplate.fileLink(v.getFilePath(), v.getBucketName())).openStream(), Boolean.FALSE);
                                } catch (Exception ex) {
                                    log.info("获取office内容错误", ex);
                                }
                            }
                            // 用户信息
                            UserDto newUserDto = new UserDto();
                            newUserDto.setId(v.getCreateById());
                            newUserDto.setRealName(v.getCreateBy());
                            DcLibrary knowledge = dcLibraryService.getById(v.getKnowledgeId());
                            if (Optional.ofNullable(knowledge).isPresent()) {
                                return documentElasticService.build(newUserDto, v, content, knowledge);
                            }
                            return null;
                        }).filter(ObjectUtil::isNotNull)
                        .collect(Collectors.toList());
                esTemplate.save(esPoList);
                //删除脏数据 就是没有路径参数的 是因为前面有个版本的删除逻辑 有问题在删除文件夹时 没有删除文件夹下面的所有数据
                libraries.stream().filter(predicate)
                        .forEach(v -> dcLibraryMapper.deleteRecycle(v.getId(), v.getType().getValue()));
            });
            messageTxt += "成功";
        } catch (Exception exception) {
            log.info("数据升级失败:", exception);
            messageTxt += "失败";
            success = Boolean.FALSE;
        } finally {
            Dict set = Dict.create().set("title", "系统升级").set("content", messageTxt);
            ReceiversDto receiversDto = new ReceiversDto().setUserId(userDto.getId()).setTenantId(userDto.getTenantId());
            InsideNotificationDto notificationDto = new InsideNotificationDto();
            notificationDto
                    .setContent(JSONObject.toJSONString(set))
                    .setDefinedReceivers(Arrays.asList(receiversDto));
            insideNotificationApi.send(notificationDto);
            //解锁
            RedisUtils redisUtils = SpringContextUtil.getBean(RedisUtils.class);
            redisUtils.del(Constant.LOCK_COMMON_KEY);
        }
        if (!success) {
            throw new BusinessException("升级失败");
        }
    }


    /**
     * 递归进行路径数据的设置
     *
     * @param dcId      当前层级的id
     * @param pathId    当前层级的路径数据
     * @param libraries 所有数据
     */
    private void knowledgeUpdate(List<DcLibrary> libraries, String dcId, List<String> pathId) {
        libraries.stream()
                .filter(e -> e.getParentId().equals(dcId))
                .forEach(e -> {
                    e.setPathId(pathId);
                    if (e.getType().equals(DcLibraryTypeEnum.directory)) {
                        List<String> newPathId = BeanUtil.copyToList(pathId, String.class);
                        newPathId.add(e.getId());
                        knowledgeUpdate(libraries, e.getId(), newPathId);
                    }
                });
    }

    /**
     * 异步清空回收站
     */
    public void asyncClearRecycle() {
        try {
            //获取数据id
            List<DcLibraryRecycle> list = dcLibraryRecycleService.list();
            DcLibraryService bean = SpringContextUtil.getBean(DcLibraryService.class);
            list.forEach(e -> bean.removeDc(e.getDcId()));
        } catch (Exception e) {
            log.info("异步清空回收站失败", e);
        }
    }


    /**
     * 异步解压文件
     *
     * @param documentMqDto 文档信息
     */
    public void asyncUnzip(DocumentMqDto documentMqDto, UserDto currentUser) {
        if (ObjectUtil.isNotEmpty(documentMqDto.getLocale())) {
            LocaleContextHolder.setLocale(documentMqDto.getLocale());
        }
        TimeInterval timer = new TimeInterval();
        String status = "成功";
        //防止线程变更
        TenantContextHolder.setTenantId(documentMqDto.getTenantId());
        DcLibraryService dcLibraryService = SpringContextUtil.getBean(DcLibraryService.class);
        DcLibrary dcLibrary = dcLibraryService.getById(documentMqDto.getDocumentId());
        final String nameSuffix = "zip";
        if (StrUtil.isBlank(dcLibrary.getNameSuffix()) || !dcLibrary.getNameSuffix().equals(nameSuffix)) {
            status = "失败,只支持zip的压缩包解压!";
        } else {
            //下载文件
            //获取文件url
            String s = ossTemplate.fileLink(dcLibrary.getFilePath(), dcLibrary.getBucketName());
            try (InputStream inputStream = new URL(s).openStream()) {
                File file = FileUtil.writeFromStream(inputStream, FileUtil.file(dcLibrary.getName()));
                //获取压缩包内容
                List<UnzipPo> unzip = ZipFileUtil.unzip(file);
                //拆分路径
                unzip.forEach(e -> {
                    String parentId = dcLibrary.getParentId();
                    //判断是否为顶级 如果不是 就需要获取上级id
                    if (!e.getTopIs()) {
                        parentId = unzip.stream().filter(v -> v.getFilePath().equals(e.getParentLevel())).findFirst().orElseThrow(() -> new BusinessException("获取上级id错误")).getDcId();
                    }
                    //文件夹直接创建
                    if (e.getIsDirectory()) {
                        DcLibraryAddReqVo reqVo = new DcLibraryAddReqVo()
                                .setFileType(DcLibraryTypeEnum.directory)
                                .setId(dcLibrary.getKnowledgeId())
                                .setParentId(parentId)
                                .setName(e.getName());
                        DcLibrary add = dcLibraryService.add(documentMqDto.getUserDto(), reqVo);
                        e.setDcId(add.getId());
                    } else {
                        //文件创建 直接上传文件 调用公共方法即可
                        BaseFile baseFile = ossTemplate.putFile(BUCKET_NAME, SpringContextUtil.getApplicationContextName(), e.getName(), e.getInputStream());
                        dcLibraryService.fileToSave(parentId, baseFile, null, currentUser);
                        try {
                            e.getInputStream().close();
                        } catch (Exception exception) {
                            //关闭流失败
                            log.info("解压文件保存文件后关闭流失败", exception);
                        }
                    }
                });
                boolean delete = file.delete();
                log.info("解压临时文件删除是否成功:{}", delete);
            } catch (Exception e) {
                status = "失败";
                log.info("数据解压错误", e);
            }
        }
        this.pushMessage(dcLibrary.getName(), "解压文件", status, documentMqDto.getUserDto(), timer.intervalMs());
    }

}
