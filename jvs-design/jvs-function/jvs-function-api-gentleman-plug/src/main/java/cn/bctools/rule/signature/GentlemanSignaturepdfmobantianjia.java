package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturepdfmobantianjiaDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Gentleman signaturepdfmobantianjia.
 *
 * @author jvs
 */
@Rule(value = "PDF模板添加",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "平台需要自行把PDF模板中需要动态显示的内容以表单的方式设置好。"
)
@AllArgsConstructor
public class GentlemanSignaturepdfmobantianjia implements BaseCustomFunctionInterface<GentlemanSignaturepdfmobantianjiaDto> {

    @Override
    public Object execute(GentlemanSignaturepdfmobantianjiaDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/fileTpl/add", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturepdfmobantianjiaDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo fileName = new RuleElementVo().setName("fileName").setInfo("模板名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(fileName);
        RuleElementVo templateNo = new RuleElementVo().setName("templateNo").setInfo("模板ID").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(templateNo);
        return list;
    }
}
