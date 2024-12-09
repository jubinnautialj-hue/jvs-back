package cn.bctools.rule.ess.impl.institutionalandemployeerelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.DescribeIntegrationEmployeesRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeIntegrationEmployeesResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询企业员工信息列表",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（DescribeIntegrationEmployees）用于分页查询企业员工信息列表，支持设置过滤条件以筛选员工查询结果。"
)
@AllArgsConstructor
public class QueryEnterpriseEmployeeInformationListServiceImpl implements BaseCustomFunctionInterface<QueryEnterpriseEmployeeInformationListDto> {

    @Override
    public Object execute(QueryEnterpriseEmployeeInformationListDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeIntegrationEmployeesRequest req = BeanCopyUtil.copy(dto, DescribeIntegrationEmployeesRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));

            // 返回的resp是一个DescribeIntegrationEmployeesResponse的实例，与请求对象对应
            DescribeIntegrationEmployeesResponse resp = client.DescribeIntegrationEmployees(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
