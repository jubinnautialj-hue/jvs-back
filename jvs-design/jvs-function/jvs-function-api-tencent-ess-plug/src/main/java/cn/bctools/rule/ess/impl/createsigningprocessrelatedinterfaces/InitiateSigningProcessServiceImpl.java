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
import com.tencentcloudapi.ess.v20201111.models.StartFlowRequest;
import com.tencentcloudapi.ess.v20201111.models.StartFlowResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "模板发起合同-发起签署流程",
        group = RuleGroup.腾讯电子签,
        test = true,
        returnType = ClassType.对象,

        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口用于启动流程。它是模板发起合同的最后一步。 在创建签署流程和创建电子文档之后，用于开始整个合同流程, 推进流程进入到签署环节。"
)
@AllArgsConstructor
public class InitiateSigningProcessServiceImpl implements BaseCustomFunctionInterface<InitiateSigningProcessDto> {

    @Override
    public Object execute(InitiateSigningProcessDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        StartFlowRequest req = new StartFlowRequest();
        try {
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowId(dto.getFlowId());
            req.setCcNotifyType(dto.getCcNotifyType());
            StartFlowResponse resp = client.StartFlow(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }
    }
}
