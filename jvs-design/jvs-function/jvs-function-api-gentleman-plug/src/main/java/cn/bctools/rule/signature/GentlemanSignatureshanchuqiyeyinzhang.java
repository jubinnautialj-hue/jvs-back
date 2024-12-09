package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignatureshanchuqiyeyinzhangDto;

import java.util.Map;

/**
 * The type Gentleman signatureshanchuqiyeyinzhang.
 *
 * @author jvs
 */
@Rule(value = "删除企业印章",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "删除企业印章。“"
)
@AllArgsConstructor
public class GentlemanSignatureshanchuqiyeyinzhang implements BaseCustomFunctionInterface<GentlemanSignatureshanchuqiyeyinzhangDto> {

    @Override
    public Object execute(GentlemanSignatureshanchuqiyeyinzhangDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/deleteEntSign", dto);
    }

}
