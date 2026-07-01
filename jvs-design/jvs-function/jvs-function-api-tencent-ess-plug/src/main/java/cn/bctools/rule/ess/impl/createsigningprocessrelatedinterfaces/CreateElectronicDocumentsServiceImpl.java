package cn.bctools.rule.ess.impl.createsigningprocessrelatedinterfaces;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.ess.util.TenantUtil;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreateDocumentRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateDocumentResponse;
import com.tencentcloudapi.ess.v20201111.models.FormField;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "模板发起合同-创建电子文档",
        group = RuleGroup.腾讯电子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "注：该接口需要给对应的流程指定一个模板id，并且填充该模板中需要补充的信息。需要配置创建签署流程和发起签署流程接口使用。具体逻辑可以参考下图:"
)
@AllArgsConstructor
public class CreateElectronicDocumentsServiceImpl implements BaseCustomFunctionInterface<CreateElectronicDocumentsDto> {

    @Override
    public Object execute(CreateElectronicDocumentsDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            CreateDocumentRequest req = new CreateDocumentRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowId(dto.getFlowId());
            req.setPreviewType(dto.getPreviewType());
            req.setTemplateId(dto.getTemplateId());
            req.setClientToken(dto.getClientToken());
            try {
                String[] array = dto.getFileNames().toArray(new String[0]);
                req.setFileNames(array);
            } catch (Exception ignored) {

            }
            req.setNeedPreview(dto.getNeedPreview());
            try {
                req.setFormFields(BeanCopyUtil.copys(dto.getFormFields(), FormField.class).toArray(new FormField[dto.getFormFields().size()]));
            } catch (Exception ignored) {

            }

            CreateDocumentResponse resp = client.CreateDocument(req);
            return BeanCopyUtil.copy(resp, HashMap.class);

        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }
    }

}
