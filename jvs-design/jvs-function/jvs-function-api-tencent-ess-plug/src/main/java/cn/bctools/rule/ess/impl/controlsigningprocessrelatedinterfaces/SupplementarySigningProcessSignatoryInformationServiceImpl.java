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
import com.tencentcloudapi.ess.v20201111.models.CreateFlowApproversRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowApproversResponse;
import com.tencentcloudapi.ess.v20201111.models.FillApproverInfo;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "补充签署流程签署人信息",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "适用场景： 当通过模板或文件发起合同时，若未指定企业签署人信息，则可调用此接口补充或添加签署人。同一签署人可补充多个员工作为或签署人，最终实际签署人取决于谁先领取合同完成签署。\n" +
                "\n" +
                "限制条件：\n" +
                "\n" +
                "本企业（发起方企业）企业微信签署人仅支持通过企业微信UserId或姓名+手机号进行补充。\n" +
                "本企业（发起方企业）非企业微信签署人仅支持通过姓名+手机号进行补充。\n" +
                "他方企业仅支持通过姓名+手机号进行补充。"
)
@AllArgsConstructor
public class SupplementarySigningProcessSignatoryInformationServiceImpl implements BaseCustomFunctionInterface<SupplementarySigningProcessSignatoryInformationDto> {

    @Override
    public Object execute(SupplementarySigningProcessSignatoryInformationDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateFlowApproversRequest req = new CreateFlowApproversRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));

            req.setFlowId(dto.getFlowId());
            try {
                req.setApprovers(BeanCopyUtil.copys(dto.getApprovers(), FillApproverInfo.class).toArray(new FillApproverInfo[dto.getApprovers().size()]));
            } catch (Exception ignored) {

            }
            req.setInitiator(dto.getInitiator());
            req.setFillApproverType(dto.getFillApproverType());

            // 返回的resp是一个CreateFlowApproversResponse的实例，与请求对象对应
            CreateFlowApproversResponse resp = client.CreateFlowApprovers(req);

            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
