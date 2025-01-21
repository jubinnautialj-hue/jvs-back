package cn.bctools.rule.business.user;


import cn.bctools.auth.api.api.AuthRoleServiceApi;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author czy
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "授权用户角色",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 43,
        explain = "给选择的用户授权对应的角色信息"
)
public class UserRoleServiceImpl implements BaseCustomFunctionInterface<UserRoleDto> {

    AuthRoleServiceApi roleServiceApi;
    JvsSystemConfig jvsSystemConfig;

    @SneakyThrows
    @Override
    public Object execute(UserRoleDto dto, Map<String, Object> params) {
        if ("bctools.cn".equals(jvsSystemConfig.getDomain())) {
            throw new BusinessException("不支持操作");
        }
        roleServiceApi.setUser(dto.getRoleId(), dto.getUserIds());
        return true;
    }

}

