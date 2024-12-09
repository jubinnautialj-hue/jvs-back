package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignaturechexiaopiliangqianyuefaqiDto;

import java.util.Map;

/**
 * The type Gentleman signaturechexiaopiliangqianyuefaqi.
 *
 * @author jvs
 */
@Rule(value = "撤销批量签约发起",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "在合同还未签署完成的情况下才可以通过该接口撤销合同。已经签署完成的合同不支持撤销"
)
@AllArgsConstructor
public class GentlemanSignaturechexiaopiliangqianyuefaqi implements BaseCustomFunctionInterface<GentlemanSignaturechexiaopiliangqianyuefaqiDto> {

    @Override
    public Object execute(GentlemanSignaturechexiaopiliangqianyuefaqiDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/batchSignCancel", dto);
    }

}
