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
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowBriefsRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowBriefsResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询流程基础信息",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "查询流程基础信息 适用场景：可用于主动查询某个合同流程的签署状态信息。可以配合回调通知使用。\n" +
                "\n" +
                "注: 每个企业限制日调用量限制：100W，当日超过此限制后再调用接口返回错误"
)
@AllArgsConstructor
public class QueryProcessBasicInformationServiceImpl implements BaseCustomFunctionInterface<QueryProcessBasicInformationDto> {

    @Override
    public Object execute(QueryProcessBasicInformationDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeFlowBriefsRequest req = new DescribeFlowBriefsRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowIds(dto.getFlowIds().toArray(new String[0]));
            // 返回的resp是一个DescribeFlowBriefsResponse的实例，与请求对象对应
            DescribeFlowBriefsResponse resp = client.DescribeFlowBriefs(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
