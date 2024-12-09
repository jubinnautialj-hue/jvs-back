package cn.bctools.rule.ess.impl.billingrelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.DescribeBillUsageDetailRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeBillUsageDetailResponse;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询企业计费使用情况",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "通过此接口（DescribeBillUsageDetail）查询该企业的套餐消耗详情。"
)
@AllArgsConstructor
public class QueryTheUsageOfEnterpriseBillingServiceImpl implements BaseCustomFunctionInterface<QueryTheUsageOfEnterpriseBillingDto> {

    @Override
    public Object execute(QueryTheUsageOfEnterpriseBillingDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeBillUsageDetailRequest req = BeanCopyUtil.copy(dto, DescribeBillUsageDetailRequest.class);
            // 返回的resp是一个DescribeBillUsageDetailResponse的实例，与请求对象对应
            DescribeBillUsageDetailResponse resp = client.DescribeBillUsageDetail(req);
            return BeanCopyUtil.copy(resp, HashMap.class);

        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
