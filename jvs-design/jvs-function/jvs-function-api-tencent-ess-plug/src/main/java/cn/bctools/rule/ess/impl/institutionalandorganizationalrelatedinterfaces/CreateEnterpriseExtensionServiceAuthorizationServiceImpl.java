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
import com.tencentcloudapi.ess.v20201111.models.CreateExtendedServiceAuthInfosRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateExtendedServiceAuthInfosResponse;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "创建企业扩展服务授权",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "创建企业扩展服务授权，当前仅支持授权 “企业自动签” 和 “批量签署” 给企业员工。 该接口作用和电子签控制台 企业设置-扩展服务-企业自动签署和批量签署授权 两个模块功能相同，可通过该接口授权给企业员工。\n" +
                "\n" +
                "注：“企业自动签授权”支持集团代子企业操作，请联系运营开通此功能。"
)
@AllArgsConstructor
public class CreateEnterpriseExtensionServiceAuthorizationServiceImpl implements BaseCustomFunctionInterface<CreateEnterpriseExtensionServiceAuthorizationDto> {

    @Override
    public Object execute(CreateEnterpriseExtensionServiceAuthorizationDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateExtendedServiceAuthInfosRequest req = new CreateExtendedServiceAuthInfosRequest();

            // 返回的resp是一个CreateExtendedServiceAuthInfosResponse的实例，与请求对象对应
            CreateExtendedServiceAuthInfosResponse resp = client.CreateExtendedServiceAuthInfos(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
