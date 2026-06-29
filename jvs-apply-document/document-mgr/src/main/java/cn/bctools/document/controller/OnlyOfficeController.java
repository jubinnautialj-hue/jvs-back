package cn.bctools.document.controller;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.DcLibraryLog;
import cn.bctools.document.entity.DcTemplate;
import cn.bctools.document.entity.DcTemplateType;
import cn.bctools.document.entity.enums.DcLibraryLogOperationTypeEnum;
import cn.bctools.document.listener.es.DocumentMqEvent;
import cn.bctools.document.message.MessageFactory;
import cn.bctools.document.receiver.DocumentConsumer;
import cn.bctools.document.service.DcLibraryLogService;
import cn.bctools.document.service.DcLibraryService;
import cn.bctools.document.service.DcTemplateService;
import cn.bctools.document.service.DcTemplateTypeService;
import cn.bctools.document.util.FileOssUtils;
import cn.bctools.log.annotation.Log;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

/**
 * onlyoffice回调的地址
 *
 * @author xiaohui
 */
@Slf4j
@Api(tags = "不需要授权请求的接口")
@RestController
@RequestMapping(value = "/no/auth/only/office")
@AllArgsConstructor
public class OnlyOfficeController {
    private final DcLibraryService dcLibraryService;
    private final OssTemplate ossTemplate;
    private final DcTemplateService dcTemplateService;
    private final DcTemplateTypeService dcTemplateTypeService;
    private final DcLibraryLogService dcLibraryLogService;
    private final MessageFactory messageFactory;
    private final DocumentConsumer documentConsumer;


    @Log
    @PostMapping
    @ApiOperation("保存时的回调地址")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据id"),
            @ApiImplicitParam(name = "userId", value = "用户id"),
            @ApiImplicitParam(name = "userName", value = "用户名称"),
            @ApiImplicitParam(name = "isTemple", value = "是否为模板")
    })
    @SneakyThrows
    public String save(String id, String userId, String userName, HttpServletRequest request, Boolean isTemple, String tenantId, String locale) {
        //设置语言
        if (StrUtil.isNotBlank(locale)) {
            Locale locale1 = new Locale(locale);
            LocaleContextHolder.setLocale(locale1);
        }
        Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A");
        String body = scanner.hasNext() ? scanner.next() : "";
        log.info("回调时的入参为:{}", body);
        JSONObject jsonObject = JSONObject.parseObject(body);
        int code = 1;
        int status = jsonObject.getIntValue("status");
        if (status == 1 || status == 4) {
            return "{\"error\":0}";
        }
        if (Arrays.asList(2, 3, 6, 7).contains(status)) {
            String downloadUri = (String) jsonObject.get("url");
            if (StrUtil.isBlank(downloadUri)) {
                return "{\"error\":文件地址为空}";
            }
            //下载文件
            InputStream stream = FileOssUtils.getInputStream(downloadUri);
            if (stream == null) {
                log.info("onlyOffice回调获取流文件失败");
                return "{\"error\":获取流文件错误}";
            }
            //手动设置租户
            TenantContextHolder.setTenantId(tenantId);
            //区分是否为模板
            if (!isTemple) {
                DcLibrary dcLibrary = dcLibraryService.getById(id);
                if (ObjectUtil.isNotNull(dcLibrary)) {
                    String originalName = FileOssUtils.getMultipartFileName(dcLibrary) + "." + dcLibrary.getNameSuffix();
                    //同步minio与数据库
                    DcLibrary know = dcLibraryService.get(dcLibrary.getKnowledgeId());
                    BaseFile baseFile = ossTemplate.putFile(originalName, stream, know.getName(), dcLibrary.getName());
                    dcLibrary.setBucketName(baseFile.getBucketName());
                    dcLibrary.setFilePath(baseFile.getFileName());
                    dcLibrary.setSize(baseFile.getSize());
                    dcLibraryService.updateById(dcLibrary);
                    //同步ES
                    //入队列
                    DocumentMqEvent documentMqEvent = new DocumentMqEvent()
                            .setDocumentId(dcLibrary.getId())
                            .setContent(dcLibrary.getContent())
                            .setTenantId(TenantContextHolder.getTenantId())
                            .setUserDto(new UserDto().setRealName(userName).setId(userId));
                    documentConsumer.sendFileToEs(documentMqEvent);
                    //日志入库
                    DcLibraryLog libraryLog = new DcLibraryLog()
                            .setDcLibraryId(id)
                            .setOperationType(DcLibraryLogOperationTypeEnum.UPDATE.getValue())
                            .setKnowledgeId(dcLibrary.getKnowledgeId())
                            .setType(dcLibrary.getType())
                            .setDcName(dcLibrary.getName())
                            .setNameSuffix(dcLibrary.getNameSuffix())
                            .setParentId(dcLibrary.getParentId())
                            .setUserId(userId)
                            .setUserName(userName);
                    dcLibraryLogService.save(libraryLog);
                    UserDto userDto = new UserDto().setRealName(userName).setId(userId).setTenantId(tenantId);
                    //消息推送
                    messageFactory.messagePush(DcLibraryLogOperationTypeEnum.UPDATE, dcLibrary, userDto);
                    code = 0;
                }
            } else {
                DcTemplate byId = dcTemplateService.getById(id);
                if (ObjectUtil.isNotNull(byId)) {
                    //获取模板类型名称
                    DcTemplateType templateType = dcTemplateTypeService.getById(byId.getTypeId());
                    //修改模板数据
                    String multipartFileName = FileOssUtils.getMultipartFileName(byId.getType()) + "." + byId.getNameSuffix();
                    String typeName = byId.getName();
                    if (ObjectUtil.isNotNull(templateType)) {
                        typeName = templateType.getTypeName();
                    }
                    BaseFile baseFile = ossTemplate.putFile(multipartFileName, stream, typeName, byId.getName());
                    //入库
                    dcTemplateService.update(new UpdateWrapper<DcTemplate>().lambda()
                            .eq(DcTemplate::getId, id)
                            .set(DcTemplate::getBucketName, baseFile.getBucketName())
                            .set(DcTemplate::getFilePath, baseFile.getFileName()));
                    code = 0;
                }
            }
            //关闭流 关闭后需要删除文件
            stream.close();
        }
        return "{\"error\":" + code + "}";
    }
}
