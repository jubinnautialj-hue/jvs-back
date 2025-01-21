package cn.bctools.rule.business.user;


import cn.bctools.auth.api.api.AuthUserServiceApi;
import cn.bctools.auth.api.dto.SaveUserDto;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.jvs.JvsSystemConfig;
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
@Rule(value = "新增用户",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 43,
        explain = "根据用户填写的对应信息新增一个额外的用户对象"
)
public class AddOrUpdateUserServiceImpl implements BaseCustomFunctionInterface<AddOrUpdateUserDto> {
    AuthUserServiceApi userServiceApi;
    JvsSystemConfig jvsSystemConfig;

    @Override
    public Object execute(AddOrUpdateUserDto dto, Map<String, Object> params) {
        SaveUserDto copy = BeanCopyUtil.copy(dto, SaveUserDto.class);
        if ("bctools.cn".equals(jvsSystemConfig.getDomain())) {
            throw new BusinessException("不支持操作");
        }
        return userServiceApi.saveOrUpdate(copy).getData();
    }

}

