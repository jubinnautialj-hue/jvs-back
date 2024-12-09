package cn.bctools.rule.ess.impl.embedablepagerelatedinterfaces;

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
@Rule(value = "创建发起流程web页面",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "适用场景：通过该接口（CreatePrepareFlow）传入合同文件/模板编号及签署人信息，可获得发起流程的可嵌入页面，在页面完成签署控件等信息的编辑与确认后，快速发起流程。\n" +
                "注：该接口包含模板/文件发起流程的全部功能，调用接口后不会立即发起，需在可嵌入页面点击按钮进行发起流程。"
)
@AllArgsConstructor
public class CreateWebPageForInitiatingTheProcessServiceImpl implements BaseCustomFunctionInterface<CreateWebPageForInitiatingTheProcessDto> {

    @Override
    public Object execute(CreateWebPageForInitiatingTheProcessDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreatePrepareFlowRequest req = new CreatePrepareFlowRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            try {
                req.setApprovers(BeanCopyUtil.copys(dto.getApprovers(), FlowCreateApprover.class).toArray(new FlowCreateApprover[dto.getApprovers().size()]));
            } catch (Exception ignored) {

            }
            req.setResourceId(dto.getResourceId());
            req.setFlowName(dto.getFlowName());
            req.setUnordered(dto.getUnordered());
            req.setDeadline(dto.getDeadline());
            req.setUserFlowTypeId(dto.getUserFlowTypeId());
            req.setIntelligentStatus(dto.getIntelligentStatus());
            req.setResourceType(dto.getResourceType());
            req.setComponents(BeanCopyUtil.copy(dto.getComponents(), Component.class));
            req.setFlowOption(BeanCopyUtil.copy(dto.getFlowOption(), CreateFlowOption.class));
            req.setNeedSignReview(dto.getNeedSignReview());
            req.setNeedCreateReview(dto.getNeedCreateReview());
            req.setUserData(dto.getUserData());
            req.setFlowId(dto.getFlowId());
            req.setFlowType(dto.getFlowType());
            // 返回的resp是一个CreatePrepareFlowResponse的实例，与请求对象对应
            CreatePrepareFlowResponse resp = client.CreatePrepareFlow(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
