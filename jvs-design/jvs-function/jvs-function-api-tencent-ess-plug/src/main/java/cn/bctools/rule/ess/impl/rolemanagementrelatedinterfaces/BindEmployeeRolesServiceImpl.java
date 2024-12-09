package cn.bctools.rule.ess.impl.rolemanagementrelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.CreateIntegrationUserRolesRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateIntegrationUserRolesResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "绑定员工角色",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口用于赋予员工指定的角色权限，如需解绑请使用 DeleteIntegrationRoleUsers 接口。"
)
@AllArgsConstructor
public class BindEmployeeRolesServiceImpl implements BaseCustomFunctionInterface<BindEmployeeRolesDto> {

    @Override
    public Object execute(BindEmployeeRolesDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateIntegrationUserRolesRequest req = new CreateIntegrationUserRolesRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setUserIds(dto.getUserIds().toArray(new String[0]));
            req.setRoleIds(dto.getRoleIds().toArray(new String[0]));
            // 返回的resp是一个CreateIntegrationUserRolesResponse的实例，与请求对象对应
            CreateIntegrationUserRolesResponse resp = client.CreateIntegrationUserRoles(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
