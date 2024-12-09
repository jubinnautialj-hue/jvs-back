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
import com.tencentcloudapi.ess.v20201111.models.*;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "发起解除协议",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "发起解除协议的主要应用场景为：基于一份已经签署的合同（签署流程），进行解除操作。 解除协议的模板是官方提供 ，经过提供法务审核，暂不支持自定义。\n" +
                "\n" +
                "注意：\n" +
                "\n" +
                "原合同必须签署完成后才能发起解除协议。\n" +
                "只有原合同企业类型的参与人才能发起解除协议，个人参与方不能发起解除协议。\n" +
                "原合同个人类型参与人必须是解除协议的参与人，不能更换其他第三方个人参与解除协议。\n" +
                "如果原合同企业参与人无法参与解除协议，可以指定同企业具有同等权限的企业员工代为处理。\n" +
                "发起解除协议同发起其他企业合同一样，也会参与合同扣费，扣费标准同其他类型合同。\n" +
                "在解除协议发起之后，原合同的状态将转变为解除中。一旦解除协议签署完毕，原合同及解除协议均变为已解除状态。"
)
@AllArgsConstructor
public class InitiateTerminationOfAgreementServiceImpl implements BaseCustomFunctionInterface<InitiateTerminationOfAgreementDto> {

    @Override
    public Object execute(InitiateTerminationOfAgreementDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateReleaseFlowRequest req = new CreateReleaseFlowRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setDeadline(dto.getDeadline());
            req.setReliveInfo(BeanCopyUtil.copy(dto.getReliveInfo(), RelieveInfo.class));
            req.setUserData(dto.getUserData());
            req.setNeedRelievedFlowId(dto.getNeedRelievedFlowId());
            try {
                req.setReleasedApprovers(BeanCopyUtil.copys(dto.getReleasedApprovers(), ReleasedApprover.class).toArray(new ReleasedApprover[dto.getReleasedApprovers().size()]));
            } catch (Exception ignored) {

            }
            // 返回的resp是一个CreateReleaseFlowResponse的实例，与请求对象对应
            CreateReleaseFlowResponse resp = client.CreateReleaseFlow(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
