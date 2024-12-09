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
import com.tencentcloudapi.ess.v20201111.models.ModifyIntegrationRoleRequest;
import com.tencentcloudapi.ess.v20201111.models.ModifyIntegrationRoleResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Update enterprise role service.
 *
 * @author gl
 */
@Rule(value = "更新企业角色", group = RuleGroup.腾讯电子签, test = true, enable = true, returnType = ClassType.对象, testShowEnum = TestShowEnum.JSON, order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（ModifyIntegrationRole）用来更新企业自定义的SaaS角色或集团角色。\n" + "\n" + "适用场景1：更新当前企业的自定义SaaS角色或集团角色，并且更新时不进行角色中权限的更新（PermissionGroups 参数不传）。\n" + "\n" + "适用场景2：更新当前企业的自定义SaaS角色或集团角色，并且更新时进行角色中权限的设置（PermissionGroups 参数要传），权限树内容 PermissionGroups 可参考接口 DescribeIntegrationRoles 的输出。此处注意权限树内容可能会更新，需尽量拉取最新的权限树内容，并且权限树内容 PermissionGroups 必须是一颗完整的权限树。\n" + "\n" + "适用场景3：更新集团角色管理的子企业列表，可通过设置 SubOrganizationIds 参数达到此效果。\n" + "\n" + "适用场景4：主企业代理子企业操作的场景，需要设置Agent参数，并且ProxyOrganizationId设置为子企业的id即可。\n" + "\n" + "注意事项：SaaS角色和集团角色对应的权限树是不一样的。")
@AllArgsConstructor
public class UpdateEnterpriseRoleServiceImpl implements BaseCustomFunctionInterface<UpdateEnterpriseRoleDto> {

    @Override
    public Object execute(UpdateEnterpriseRoleDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ModifyIntegrationRoleRequest req = BeanCopyUtil.copy(dto, ModifyIntegrationRoleRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            // 返回的resp是一个ModifyIntegrationRoleResponse的实例，与请求对象对应
            ModifyIntegrationRoleResponse resp = client.ModifyIntegrationRole(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }
    }

}
