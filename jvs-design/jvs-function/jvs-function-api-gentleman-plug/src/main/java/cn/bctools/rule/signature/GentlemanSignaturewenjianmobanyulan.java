package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignaturewenjianmobanyulanDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signaturewenjianmobanyulan.
 * @author jvs
 */
@Rule(value = "文件模板预览",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "（pdf/word）文件模板预览"
)
@AllArgsConstructor
public class GentlemanSignaturewenjianmobanyulan implements BaseCustomFunctionInterface<GentlemanSignaturewenjianmobanyulanDto> {

    @Override
    public Object execute(GentlemanSignaturewenjianmobanyulanDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/tmpl/preTmplPdf", dto);
    }
    
}
