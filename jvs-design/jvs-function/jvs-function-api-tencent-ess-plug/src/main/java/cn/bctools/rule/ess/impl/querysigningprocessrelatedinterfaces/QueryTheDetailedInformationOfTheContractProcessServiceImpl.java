package cn.bctools.rule.ess.impl.querysigningprocessrelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowInfoRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowInfoResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询合同流程的详情信息",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口用于查询合同流程的详情信息，支持查询多个（数量不能超过100）。\n" +
                "\n" +
                "适用场景：可用于主动查询某个合同详情信息。"
)
@AllArgsConstructor
public class QueryTheDetailedInformationOfTheContractProcessServiceImpl implements BaseCustomFunctionInterface<QueryTheDetailedInformationOfTheContractProcessDto> {

    @Override
    public Object execute(QueryTheDetailedInformationOfTheContractProcessDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        DescribeFlowInfoRequest req = new DescribeFlowInfoRequest();
        try {
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowGroupId(dto.getFlowGroupId());
            req.setFlowIds(dto.getFlowIds().toArray(new String[0]));
            req.setFlowGroupId(dto.getFlowGroupId());
            DescribeFlowInfoResponse resp = client.DescribeFlowInfo(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }
    }
}
