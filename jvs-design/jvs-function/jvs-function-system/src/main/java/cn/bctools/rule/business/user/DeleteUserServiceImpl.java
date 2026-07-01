package cn.bctools.rule.business.user;

import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SaveUserDto;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@AllArgsConstructor
@Rule(value = "删除用户",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 43,
        explain = "提交用户 Id，删除指定用户，如需要彻底删除需要在后台操作",
        demoDisabled = true
)
public class DeleteUserServiceImpl implements BaseCustomFunctionInterface<AddOrUpdateUserDto> {
    AuthUserServiceApi userServiceApi;

    @Override
    public Object execute(AddOrUpdateUserDto dto, Map<String, Object> params) {
        return userServiceApi.deleteUser(dto.getUserId(), TenantContextHolder.getTenantId()).getData();
    }

}

