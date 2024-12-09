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
import com.tencentcloudapi.ess.v20201111.models.ModifyIntegrationDepartmentRequest;
import com.tencentcloudapi.ess.v20201111.models.ModifyIntegrationDepartmentResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "更新企业部门信息",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（ModifyIntegrationDepartment）用于更新企业的部门信息，支持更新部门名称、客户系统部门ID和部门序号等信息。"
)
@AllArgsConstructor
public class UpdateEnterpriseDepartmentInformationServiceImpl implements BaseCustomFunctionInterface<UpdateEnterpriseDepartmentInformationDto> {

    @Override
    public Object execute(UpdateEnterpriseDepartmentInformationDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());

        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ModifyIntegrationDepartmentRequest req = BeanCopyUtil.copy(dto, ModifyIntegrationDepartmentRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            // 返回的resp是一个ModifyIntegrationDepartmentResponse的实例，与请求对象对应
            ModifyIntegrationDepartmentResponse resp = client.ModifyIntegrationDepartment(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
