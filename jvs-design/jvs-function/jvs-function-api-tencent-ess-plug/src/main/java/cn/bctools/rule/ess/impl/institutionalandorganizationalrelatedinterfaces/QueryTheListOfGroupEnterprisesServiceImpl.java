package cn.bctools.rule.ess.impl.institutionalandorganizationalrelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.DescribeOrganizationGroupOrganizationsRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeOrganizationGroupOrganizationsResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询集团企业列表",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此API接口用来查询加入集团的成员企业信息 适用场景：子企业在加入集团后，主企业可能通过此接口获取到所有的子企业列表，方便进行展示和统计"
)
@AllArgsConstructor
public class QueryTheListOfGroupEnterprisesServiceImpl implements BaseCustomFunctionInterface<QueryTheListOfGroupEnterprisesDto> {

    @Override
    public Object execute(QueryTheListOfGroupEnterprisesDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeOrganizationGroupOrganizationsRequest req = BeanCopyUtil.copy(dto, DescribeOrganizationGroupOrganizationsRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));

            // 返回的resp是一个DescribeOrganizationGroupOrganizationsResponse的实例，与请求对象对应
            DescribeOrganizationGroupOrganizationsResponse resp = client.DescribeOrganizationGroupOrganizations(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
