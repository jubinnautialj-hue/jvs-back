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
import com.tencentcloudapi.ess.v20201111.models.*;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "用PDF文件创建签署流程",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（CreateFlowByFiles）用来通过上传后的pdf资源编号来创建待签署的合同流程。\n" +
                "适用场景1：适用非制式的合同文件签署。一般开发者自己有完整的签署文件，可以通过该接口传入完整的PDF文件及流程信息生成待签署的合同流程。\n" +
                "适用场景2：可通过该接口传入制式合同文件，同时在指定位置添加签署控件。可以起到接口创建临时模板的效果。如果是标准的制式文件，建议使用模板功能生成模板ID进行合同流程的生成。\n" +
                "注意事项：该接口需要依赖“多文件上传”接口生成pdf资源编号（FileIds）进行使用。"
)
@AllArgsConstructor
public class CreateSigningProcessUsingPDFFilesServiceImpl implements BaseCustomFunctionInterface<CreateSigningProcessUsingPDFFilesDto> {

    @Override
    public Object execute(CreateSigningProcessUsingPDFFilesDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateFlowByFilesRequest req = new CreateFlowByFilesRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowName(dto.getFlowName());
            try {
                req.setApprovers(BeanCopyUtil.copys(dto.getApprovers(), ApproverInfo.class).toArray(new ApproverInfo[dto.getApprovers().size()]));
            } catch (Exception ignored) {

            }
            req.setFileIds(dto.getFileIds().toArray(new String[0]));
            req.setFlowDescription(dto.getFlowDescription());
            req.setFlowType(dto.getFlowType());
            try {
                req.setComponents(BeanCopyUtil.copys(dto.getComponents(), Component.class).toArray(new Component[dto.getComponents().size()]));
            } catch (Exception ignored) {

            }
            try {
                req.setCcInfos(BeanCopyUtil.copys(dto.getCcInfos(), CcInfo.class).toArray(new CcInfo[dto.getCcInfos().size()]));
            } catch (Exception ignored) {

            }
            req.setCcNotifyType(dto.getCcNotifyType());
            req.setNeedPreview(dto.getNeedPreview());
            req.setPreviewType(dto.getPreviewType());
            req.setDeadline(dto.getDeadline());
            req.setRemindedOn(dto.getRemindedOn());
            req.setUnordered(dto.getUnordered());
            req.setCustomShowMap(dto.getCustomShowMap());
            req.setNeedSignReview(dto.getNeedSignReview());
            req.setApproverVerifyType(dto.getApproverVerifyType());
            req.setSignBeanTag(dto.getSignBeanTag());
            req.setAutoSignScene(dto.getAutoSignScene());

            // 返回的resp是一个CreateFlowByFilesResponse的实例，与请求对象对应
            CreateFlowByFilesResponse resp = client.CreateFlowByFiles(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
