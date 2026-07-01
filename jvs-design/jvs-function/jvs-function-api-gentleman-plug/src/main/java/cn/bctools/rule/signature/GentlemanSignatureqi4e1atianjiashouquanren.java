package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignatureqi4e1atianjiashouquanrenDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signatureqi 4 e 1 atianjiashouquanren.
 *
 * @author jvs
 */
@Rule(value = "企业添加或修改授权人",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "此接口可以满足企业多个授权人场景。"
)
@AllArgsConstructor
public class GentlemanSignatureqi4e1atianjiashouquanren implements BaseCustomFunctionInterface<GentlemanSignatureqi4e1atianjiashouquanrenDto> {

    @Override
    public Object execute(GentlemanSignatureqi4e1atianjiashouquanrenDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ent/certigier/put", dto);
    }

}
