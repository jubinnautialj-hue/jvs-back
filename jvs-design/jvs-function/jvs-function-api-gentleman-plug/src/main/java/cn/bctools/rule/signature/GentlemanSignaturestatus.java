package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignaturestatusDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signaturestatus.
 *
 * @author jvs
 */
@Rule(value = "签约状态查询",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "当只传applyNo表示查看的是整个合同的状态。如加入签约方信息表示查询的是合同中相应签约方的状态”"
)
@AllArgsConstructor
public class GentlemanSignaturestatus implements BaseCustomFunctionInterface<GentlemanSignaturestatusDto> {

    @Override
    public Object execute(GentlemanSignaturestatusDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/status", dto);
    }

}
