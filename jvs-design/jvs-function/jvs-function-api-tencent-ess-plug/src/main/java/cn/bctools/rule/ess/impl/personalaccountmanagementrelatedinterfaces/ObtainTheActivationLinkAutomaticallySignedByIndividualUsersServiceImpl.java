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
import com.tencentcloudapi.ess.v20201111.models.CreateUserAutoSignEnableUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateUserAutoSignEnableUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "获取个人用户自动签的开通链接",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "获取个人用户自动签的开通链接。\n" +
                "\n" +
                "注意: 处方单等特殊场景专用，此接口为白名单功能，使用前请联系对接的客户经理沟通。"
)
@AllArgsConstructor
public class ObtainTheActivationLinkAutomaticallySignedByIndividualUsersServiceImpl implements BaseCustomFunctionInterface<ObtainTheActivationLinkAutomaticallySignedByIndividualUsersDto> {

    @Override
    public Object execute(ObtainTheActivationLinkAutomaticallySignedByIndividualUsersDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateUserAutoSignEnableUrlRequest req = BeanCopyUtil.copy(dto, CreateUserAutoSignEnableUrlRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));

            // 返回的resp是一个CreateUserAutoSignEnableUrlResponse的实例，与请求对象对应
            CreateUserAutoSignEnableUrlResponse resp = client.CreateUserAutoSignEnableUrl(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
