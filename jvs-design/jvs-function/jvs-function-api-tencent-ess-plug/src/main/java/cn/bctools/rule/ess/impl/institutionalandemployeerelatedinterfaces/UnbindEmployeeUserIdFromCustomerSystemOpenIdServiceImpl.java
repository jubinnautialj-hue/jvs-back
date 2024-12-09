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
import com.tencentcloudapi.ess.v20201111.models.UnbindEmployeeUserIdWithClientOpenIdRequest;
import com.tencentcloudapi.ess.v20201111.models.UnbindEmployeeUserIdWithClientOpenIdResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "解除员工UserId与客户系统OpenId的绑定",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（UnbindEmployeeUserIdWithClientOpenId）用于解除电子签系统员工UserId与客户系统员工OpenId之间的绑定关系。\n" +
                "\n" +
                "注：在调用此接口时，需确保OpenId已通过调用BindEmployeeUserIdWithClientOpenId接口与电子签系统的UserId绑定过。若OpenId未经过绑定，则无法使用此接口进行解绑操作。"
)
@AllArgsConstructor
public class UnbindEmployeeUserIdFromCustomerSystemOpenIdServiceImpl implements BaseCustomFunctionInterface<UnbindEmployeeUserIdFromCustomerSystemOpenIdDto> {

    @Override
    public Object execute(UnbindEmployeeUserIdFromCustomerSystemOpenIdDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            UnbindEmployeeUserIdWithClientOpenIdRequest req = BeanCopyUtil.copy(dto, UnbindEmployeeUserIdWithClientOpenIdRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            // 返回的resp是一个UnbindEmployeeUserIdWithClientOpenIdResponse的实例，与请求对象对应
            UnbindEmployeeUserIdWithClientOpenIdResponse resp = client.UnbindEmployeeUserIdWithClientOpenId(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
