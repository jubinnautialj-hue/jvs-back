package cn.bctools.design.rule.impl.form;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author zjvs
 */
@Service
@AllArgsConstructor
@Rule(value = "表单回显",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 5,
        explain = "通过表单字段结构组装数据后回显"
)
public class FormStructureServiceImpl implements BaseCustomFunctionInterface<FormStructureDto> {

    @Override
    public Object execute(FormStructureDto dto, Map<String, Object> params) {
        //这里结构已经转换过了不用管
        //直接将内容转换为对象始可
        return dto.getBody();
    }

}
