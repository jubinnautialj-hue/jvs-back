package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignatureqi4e1ashanchushouquanrenDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signatureqi 4 e 1 ashanchushouquanren.
 *
 * @author jvs
 */
@Rule(value = "企业删除授权人",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "删除授权人"

)
@AllArgsConstructor
public class GentlemanSignatureqi4e1ashanchushouquanren implements BaseCustomFunctionInterface<GentlemanSignatureqi4e1ashanchushouquanrenDto> {

    @Override
    public Object execute(GentlemanSignatureqi4e1ashanchushouquanrenDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ent/certigier/delete", dto);
    }

}
