package cn.bctools.rule.dingding.media;


import cn.bctools.common.enums.SysConfigDing;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dingding.utils.DingUtils;
import cn.bctools.rule.dingding.work.DingMsMediaIdOption;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ResourceManagementInterface;
import cn.bctools.rule.service.ResourceType;
import cn.hutool.http.HttpUtil;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMediaUploadRequest;
import com.dingtalk.api.response.OapiMediaUploadResponse;
import com.taobao.api.ApiException;
import com.taobao.api.FileItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * @author jvs
 * 钉钉的媒体资源上传
 * The type Ding upload.
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "上传媒体文件", group = RuleGroup.钉钉平台, test = true, returnType = ClassType.对象, testShowEnum = TestShowEnum.JSON, order = 3,
//        iconUrl = "rule-yinxingqiasanyaosurenzheng",
        explain = "调用本接口，上传图片、语音媒体资源文件以及普通文件，接口返回媒体资源标识media_id")
public class DingUploadImpl implements BaseCustomFunctionInterface<DingUploadDto> {

    /**
     * The Resource management interface.
     */
    ResourceManagementInterface resourceManagementInterface;

    @Override
    public Object execute(DingUploadDto dto, Map<String, Object> params) {
        //根据用户Id获取用户钉钉扩展uid
        SysConfigDing sysConfigDing = DingUtils.getSysConfigDing(TenantContextHolder.getTenantId());
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/media/upload");
            OapiMediaUploadRequest req = new OapiMediaUploadRequest();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            HttpUtil.download(dto.getMedia(), byteArrayOutputStream, true);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            FileItem media = new FileItem(dto.getFileName(), new ByteArrayInputStream(byteArray));
            req.setType(dto.getType());
            req.setMedia(media);
            OapiMediaUploadResponse rsp = client.execute(req, DingUtils.getAccessToken(sysConfigDing));
            if (rsp.isSuccess()) {
                //保存资源文件
                DingMsMediaIdOption dingMsMediaIdOption = new DingMsMediaIdOption().setMediaId(rsp.getMediaId()).setName(dto.getFileName());
                resourceManagementInterface.saveNodeResource(dto.getFileName(), ResourceType.钉钉文件资源, rsp.getMediaId(), dingMsMediaIdOption);
                return rsp.getMediaId();
            } else {
                throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", rsp.getMessage(), rsp.getRequestId(), rsp.getErrorCode()));
            }
        } catch (ApiException e) {
            log.error("钉钉三方调用异常", e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage()));
        }
    }
}
