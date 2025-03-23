package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignatureaddCasePartyDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signatureadd case party.
 *
 * @author jvs
 */
@Rule(value = "修改仲裁申请",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过此接口可以修改已提交的案件信息\n" +
                "\n" +
                "注：仲裁案件状态为“待提交”、“已驳回”时可修改。"
)
@AllArgsConstructor
public class GentlemanSignatureaddCaseParty implements BaseCustomFunctionInterface<GentlemanSignatureaddCasePartyDto> {

    @Override
    public Object execute(GentlemanSignatureaddCasePartyDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ebqarb/edit", dto);
    }

}
