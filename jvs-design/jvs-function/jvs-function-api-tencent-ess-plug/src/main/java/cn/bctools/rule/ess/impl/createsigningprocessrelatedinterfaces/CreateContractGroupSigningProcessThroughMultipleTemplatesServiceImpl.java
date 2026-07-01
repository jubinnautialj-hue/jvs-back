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
@Rule(value = "通过多模板创建合同组签署流程",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（CreateFlowGroupByTemplates）可用于通过多个模板创建合同组签署流程。\n" +
                "\n" +
                "适用场景：该接口适用于需要一次性完成多份合同签署的情况，多份合同一般具有关联性，用户以目录的形式查看合同。"
)
@AllArgsConstructor
public class CreateContractGroupSigningProcessThroughMultipleTemplatesServiceImpl implements BaseCustomFunctionInterface<CreateContractGroupSigningProcessThroughMultipleTemplatesDto> {

    @Override
    public Object execute(CreateContractGroupSigningProcessThroughMultipleTemplatesDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateFlowGroupByTemplatesRequest req = new CreateFlowGroupByTemplatesRequest();
            // 实例化一个请求对象,每个接口都会对应一个request对象
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            try {
                req.setFlowGroupInfos(BeanCopyUtil.copys(dto.getFlowGroupInfos(), FlowGroupInfo.class).toArray(new FlowGroupInfo[dto.getFlowGroupInfos().size()]));
            } catch (Exception ignored) {

            }
            req.setFlowGroupName(dto.getFlowGroupName());
            req.setFlowGroupOptions(BeanCopyUtil.copy(dto.getFlowGroupOptions(), FlowGroupOptions.class));
            // 返回的resp是一个CreateFlowGroupByTemplatesResponse的实例，与请求对象对应
            CreateFlowGroupByTemplatesResponse resp = client.CreateFlowGroupByTemplates(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
