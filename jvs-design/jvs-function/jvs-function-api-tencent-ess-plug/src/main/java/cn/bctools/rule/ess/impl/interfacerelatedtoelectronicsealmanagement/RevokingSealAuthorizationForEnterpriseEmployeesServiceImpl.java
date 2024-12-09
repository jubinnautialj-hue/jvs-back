package cn.bctools.rule.ess.impl.interfacerelatedtoelectronicsealmanagement;

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
import com.tencentcloudapi.ess.v20201111.models.DeleteSealPoliciesRequest;
import com.tencentcloudapi.ess.v20201111.models.DeleteSealPoliciesResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "撤销企业员工的印章授权",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "本接口（DeleteSealPolicies）用于撤销企业员工持有的印章权限"
)
@AllArgsConstructor
public class RevokingSealAuthorizationForEnterpriseEmployeesServiceImpl implements BaseCustomFunctionInterface<RevokingSealAuthorizationForEnterpriseEmployeesDto> {

    @Override
    public Object execute(RevokingSealAuthorizationForEnterpriseEmployeesDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DeleteSealPoliciesRequest req = BeanCopyUtil.copy(dto, DeleteSealPoliciesRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            // 返回的resp是一个DeleteSealPoliciesResponse的实例，与请求对象对应
            DeleteSealPoliciesResponse resp = client.DeleteSealPolicies(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
