package cn.bctools.rule.business.user;


import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SaveUserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * @author czy
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "新增用户",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 43,
        explain = "根据用户填写的对应信息新增一个额外的用户对象，或根据用户 id修改用户信息",
        demoDisabled = true
)
public class AddOrUpdateUserServiceImpl implements BaseCustomFunctionInterface<AddOrUpdateUserDto> {
    AuthUserServiceApi userServiceApi;
    JvsSystemConfig jvsSystemConfig;

    @Override
    public Object execute(AddOrUpdateUserDto dto, Map<String, Object> params) {
        ArrayList<String> objects = new ArrayList<>();
        SaveUserDto copy = BeanCopyUtil.copy(dto, SaveUserDto.class);
        if (ObjectNull.isNotNull(copy.getDeptId())) {
            copy.getDeptId().stream().flatMap(e -> Arrays.stream(e.split(","))).map(String::trim).forEach(objects::add);
            copy.setDeptId(objects);
        }
        return userServiceApi.saveOrUpdate(copy).getData();
    }

}

