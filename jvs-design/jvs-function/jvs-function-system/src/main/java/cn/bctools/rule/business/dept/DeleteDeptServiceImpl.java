package cn.bctools.rule.business.dept;

import cn.bctools.auth.api.api.AuthDeptServiceApi;
import cn.bctools.auth.api.dto.SysDeptDto;
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
@Rule(value = "删除部门",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.TEXT,
        order = 43,
        explain = "根据指定部门 id删除此部门，如果部门下已有用户自动移出",
        demoDisabled = true
)
public class DeleteDeptServiceImpl implements BaseCustomFunctionInterface<DeleteDeptDto> {

    AuthDeptServiceApi deptServiceApi;

    @Override
    public Object execute(DeleteDeptDto deleteDeptDto, Map<String, Object> params) {
        return deptServiceApi.delete(deleteDeptDto.getId()).is();
    }
}
