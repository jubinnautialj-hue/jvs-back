package cn.bctools.rule.ess.impl.controlsigningprocessrelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.CreateFlowSignReviewRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowSignReviewResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "提交签署流程审批结果",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "提交签署流程审批结果的适用场景包括：\n" +
                "\n" +
                "在使用模板（CreateFlow）或文件（CreateFlowByFiles）创建签署流程时，若指定了参数NeedSignReview为true，且发起方企业作为签署方参与了流程签署，则可以调用此接口提交企业内部签署审批结果。自动签署也需要进行审核通过才会进行签署。\n" +
                "若签署流程状态正常，同一签署流程可以多次提交签署审批结果，签署时的最后一个“审批结果”有效。"
)
@AllArgsConstructor
public class SubmitApprovalResultsForTheSigningProcessServiceImpl implements BaseCustomFunctionInterface<SubmitApprovalResultsForTheSigningProcessDto> {

    @Override
    public Object execute(SubmitApprovalResultsForTheSigningProcessDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {

            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateFlowSignReviewRequest req = new CreateFlowSignReviewRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowId(dto.getFlowId());
            req.setOperateType(dto.getOperateType());
            req.setRecipientId(dto.getRecipientId());
            req.setReviewMessage(dto.getReviewMessage());
            req.setReviewType(dto.getReviewType());

            // 返回的resp是一个CreateFlowSignReviewResponse的实例，与请求对象对应
            CreateFlowSignReviewResponse resp = client.CreateFlowSignReview(req);

            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
