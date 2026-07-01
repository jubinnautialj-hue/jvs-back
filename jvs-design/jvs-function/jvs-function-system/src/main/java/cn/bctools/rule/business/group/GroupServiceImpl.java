package cn.bctools.rule.business.group;


import cn.bctools.auth.api.api.UserGroupServiceApi;
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
@Rule(value = "选择团队",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        customStructure = true,
        order = 40,
        explain = "根据传入或选择的团队id，返回此团队详细信息，包含名称、id、等信息"
)
public class GroupServiceImpl implements BaseCustomFunctionInterface<GroupDto> {

    UserGroupServiceApi userGroupServiceApi;

    @Override
    public Object execute(GroupDto userDto, Map<String, Object> params) {
        return userGroupServiceApi.getById(userDto.getGroup()).getData();
    }
}

