package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignatureaddSupplementDto;

import java.util.Map;

/**
 * The type Gentleman signatureadd supplement.
 *
 * @author jvs
 */
@Rule(value = "取消仲裁",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过此接口可以取消仲裁\n" +
                "\n" +
                "注：仲裁案件“待接受”的状态下才能取消仲裁:"
)
@AllArgsConstructor
public class GentlemanSignatureaddSupplement implements BaseCustomFunctionInterface<GentlemanSignatureaddSupplementDto> {

    @Override
    public Object execute(GentlemanSignatureaddSupplementDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ebqarb/cancel", dto);
    }

}
