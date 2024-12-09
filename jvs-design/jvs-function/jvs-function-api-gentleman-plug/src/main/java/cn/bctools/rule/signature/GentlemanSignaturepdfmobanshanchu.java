package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignaturepdfmobanshanchuDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signaturepdfmobanshanchu.
 *
 * @author jvs
 */
@Rule(value = "PDF模板删除",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "删除上传的PDF模板。"
)
@AllArgsConstructor
public class GentlemanSignaturepdfmobanshanchu implements BaseCustomFunctionInterface<GentlemanSignaturepdfmobanshanchuDto> {

    @Override
    public Object execute(GentlemanSignaturepdfmobanshanchuDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/fileTpl/del", dto);
    }

}
