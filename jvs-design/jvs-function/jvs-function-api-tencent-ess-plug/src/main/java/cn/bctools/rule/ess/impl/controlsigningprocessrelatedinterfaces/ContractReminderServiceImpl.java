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
import com.tencentcloudapi.ess.v20201111.models.CreateFlowRemindsRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowRemindsResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "合同催办",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "指定需要批量催办的签署流程ID，批量催办合同，最多100个。需要符合以下条件的合同才可被催办：\n" +
                "\n" +
                "发起合同时，签署人的NotifyType需设置为sms\n" +
                "合同中当前状态为“待签署”的签署人是催办的对象\n" +
                "每个合同只能催办一次\n" +
                "注意：该接口无法直接调用，请联系客户经理申请使用。"
)
@AllArgsConstructor
public class ContractReminderServiceImpl implements BaseCustomFunctionInterface<ContractReminderDto> {

    @Override
    public Object execute(ContractReminderDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateFlowRemindsRequest req = new CreateFlowRemindsRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowIds(dto.getFlowIds().toArray(new String[0]));

            // 返回的resp是一个CreateFlowRemindsResponse的实例，与请求对象对应
            CreateFlowRemindsResponse resp = client.CreateFlowReminds(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
