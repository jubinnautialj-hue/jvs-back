package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignaturehuoquhetongbianhaoDto;

import java.util.Map;

/**
 * The type Gentleman signaturehuoquhetongbianhao.
 *
 * @author jvs
 */
@Rule(value = "获取合同编号",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "该接口用于通过fileKey拿到applyNo合同编号"
)
@AllArgsConstructor
public class GentlemanSignaturehuoquhetongbianhao implements BaseCustomFunctionInterface<GentlemanSignaturehuoquhetongbianhaoDto> {

    @Override
    public Object execute(GentlemanSignaturehuoquhetongbianhaoDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/multiFile/applySign/applyNo", dto);
    }

}
