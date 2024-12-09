package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignaturerevokeCaseDto;

import java.util.Map;

/**
 * The type Gentleman signaturerevoke case.
 *
 * @author jvs
 */
@Rule(value = "删除仲裁",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过此接口可以删除已拒绝仲裁申请的案件\n" +
                "\n" +
                "注：仲裁案件状态为“已拒绝”才能删除。”"
)
@AllArgsConstructor
public class GentlemanSignaturerevokeCase implements BaseCustomFunctionInterface<GentlemanSignaturerevokeCaseDto> {

    @Override
    public Object execute(GentlemanSignaturerevokeCaseDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/arb/revokeCase", dto);
    }

}
