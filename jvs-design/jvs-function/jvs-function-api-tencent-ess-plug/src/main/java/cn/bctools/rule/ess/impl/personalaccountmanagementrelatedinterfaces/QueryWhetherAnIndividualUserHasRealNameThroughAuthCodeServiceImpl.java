package cn.bctools.rule.ess.impl.personalaccountmanagementrelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.DescribeThirdPartyAuthCodeRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeThirdPartyAuthCodeResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "通过AuthCode查询个人用户是否实名", group = RuleGroup.腾讯电子签, test = true, enable = true, returnType = ClassType.对象, testShowEnum = TestShowEnum.JSON, order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "通过AuthCode查询个人用户是否实名\n" + "\n" + "注意:\n" + "\n" + "此接口为合作引流场景使用，使用`有白名单限制`，使用前请联系对接的客户经理沟通。\n" + "`AuthCode 只能使用一次`，查询一次再次查询会返回错误")
@AllArgsConstructor
public class QueryWhetherAnIndividualUserHasRealNameThroughAuthCodeServiceImpl implements BaseCustomFunctionInterface<QueryWhetherAnIndividualUserHasRealNameThroughAuthCodeDto> {

    @Override
    public Object execute(QueryWhetherAnIndividualUserHasRealNameThroughAuthCodeDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeThirdPartyAuthCodeRequest req = BeanCopyUtil.copy(dto, DescribeThirdPartyAuthCodeRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            // 返回的resp是一个DescribeThirdPartyAuthCodeResponse的实例，与请求对象对应
            DescribeThirdPartyAuthCodeResponse resp = client.DescribeThirdPartyAuthCode(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
