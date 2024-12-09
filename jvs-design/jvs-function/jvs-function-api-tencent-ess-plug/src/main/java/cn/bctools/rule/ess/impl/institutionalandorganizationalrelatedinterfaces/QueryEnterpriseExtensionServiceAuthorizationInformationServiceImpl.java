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
import com.tencentcloudapi.ess.v20201111.models.DescribeExtendedServiceAuthInfosRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeExtendedServiceAuthInfosResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询企业扩展服务授权信息",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "查询企业扩展服务的开通和授权情况，当前支持查询以下内容：\n" +
                "\n" +
                "企业自动签\n" +
                "企业与港澳台居民签署合同\n" +
                "使用手机号验证签署方身份\n" +
                "骑缝章\n" +
                "批量签署能力\n" +
                "拓宽签署方年龄限制"
)
@AllArgsConstructor
public class QueryEnterpriseExtensionServiceAuthorizationInformationServiceImpl implements BaseCustomFunctionInterface<QueryEnterpriseExtensionServiceAuthorizationInformationDto> {

    @Override
    public Object execute(QueryEnterpriseExtensionServiceAuthorizationInformationDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeExtendedServiceAuthInfosRequest req = BeanCopyUtil.copy(dto, DescribeExtendedServiceAuthInfosRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));

            // 返回的resp是一个DescribeExtendedServiceAuthInfosResponse的实例，与请求对象对应
            DescribeExtendedServiceAuthInfosResponse resp = client.DescribeExtendedServiceAuthInfos(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
