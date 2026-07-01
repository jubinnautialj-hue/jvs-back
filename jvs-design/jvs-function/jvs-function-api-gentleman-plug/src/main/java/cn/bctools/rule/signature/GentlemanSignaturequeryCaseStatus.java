package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignaturequeryCaseStatusDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signaturequery case status.
 *
 * @author jvs
 */
@Rule(value = "裁决书下载",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过此接口可以下载裁决文书\n" +
                "\n" +
                "注：仲裁案件“已结案”状态下才能下载裁决书”"
)
@AllArgsConstructor
public class GentlemanSignaturequeryCaseStatus implements BaseCustomFunctionInterface<GentlemanSignaturequeryCaseStatusDto> {

    @Override
    public Object execute(GentlemanSignaturequeryCaseStatusDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ebqarb/download", dto);
    }

}
