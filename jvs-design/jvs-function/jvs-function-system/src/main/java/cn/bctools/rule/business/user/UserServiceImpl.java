package cn.bctools.rule.business.user;


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
 * @author czy
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "选择用户",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 43,
        explain = "根据传入的用户id,或选择指定的用户,返回此用户详细信息，包含名称头像等信息"
)
public class UserServiceImpl implements BaseCustomFunctionInterface<UserDto> {

    @Override
    public Object execute(UserDto dto, Map<String, Object> params) {
        cn.bctools.common.entity.dto.UserDto userById = AuthorityManagementUtils.getUserById(dto.getUser());
        return userById;
    }
}

