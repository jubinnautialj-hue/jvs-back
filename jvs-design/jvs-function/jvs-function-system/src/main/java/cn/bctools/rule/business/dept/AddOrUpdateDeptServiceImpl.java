package cn.bctools.rule.business.dept;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
import cn.bctools.common.exception.BusinessException;
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

/**
 * @author jvs
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "新增部门",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 43,
        explain = "根据用户填写的对应信息新增一个额外的部门对象，或根据部门 id修改部门信息",
        demoDisabled = true
)
public class AddOrUpdateDeptServiceImpl implements BaseCustomFunctionInterface<AddOrUpdateDeptDto> {

    AuthDeptServiceApi deptServiceApi;
    JvsSystemConfig jvsSystemConfig;

    @Override
    public Object execute(AddOrUpdateDeptDto dto, Map<String, Object> params) {
        SysDeptDto copy = BeanCopyUtil.copy(dto, SysDeptDto.class);

        if (ObjectNull.isNull(dto.getParentId())) {
            dto.setParentId(TenantContextHolder.getTenantId());
        }
        return deptServiceApi.saveOrUpdate(copy).getData();
    }

}
