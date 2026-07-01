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
import com.tencentcloudapi.ess.v20201111.models.CreateUserAutoSignSealUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateUserAutoSignSealUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "获取设置自动签印章小程序链接",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "获取设置自动签印章小程序链接。\n" +
                "\n" +
                "注意：\n" +
                "\n" +
                "需要企业开通自动签后使用。\n" +
                "仅支持已经开通了自动签的个人更换自动签印章。\n" +
                "链接有效期默认7天，最多30天。\n" +
                "该接口的链接适用于小程序端。\n" +
                "该接口不会扣除您的合同套餐，暂不参与计费。"
)
@AllArgsConstructor
public class GetTheLinkToTheAutomaticSealSigningMiniProgramServiceImpl implements BaseCustomFunctionInterface<GetTheLinkToTheAutomaticSealSigningMiniProgramDto> {

    @Override
    public Object execute(GetTheLinkToTheAutomaticSealSigningMiniProgramDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateUserAutoSignSealUrlRequest req = BeanCopyUtil.copy(dto, CreateUserAutoSignSealUrlRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            // 返回的resp是一个CreateUserAutoSignSealUrlResponse的实例，与请求对象对应
            CreateUserAutoSignSealUrlResponse resp = client.CreateUserAutoSignSealUrl(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
