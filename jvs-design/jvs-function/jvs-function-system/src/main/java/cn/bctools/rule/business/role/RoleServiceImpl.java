package cn.bctools.rule.business.role;


import cn.bctools.auth.api.dto.SysRoleDto;
import cn.bctools.oauth2.utils.AuthorityManagementUtils;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author st
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "选择角色",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        customStructure = true,
        order = 42,
        explain = "根据选择的角色或传入的角色id,返回角色的详细信息，包含角色的名称。"
)
public class RoleServiceImpl implements BaseCustomFunctionInterface<RoleDto> {

    @Override
    public Object execute(RoleDto userDto, Map<String, Object> params) {
        SysRoleDto roleById = AuthorityManagementUtils.getRoleById(userDto.getRole());
        return roleById;
    }
}

