package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturejiashizhengshibieDto;
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
 * The type Gentleman signaturejiashizhengshibie.
 *
 * @author jvs
 */
@Rule(value = "驾驶证识别",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供OCR驾驶证识别，通过上传驾驶证照片进行识别。该接口涉及到OCR识别费用，如果未购买需要联系商务进行购买。"
)
@AllArgsConstructor
public class GentlemanSignaturejiashizhengshibie implements BaseCustomFunctionInterface<GentlemanSignaturejiashizhengshibieDto> {

    @Override
    public Object execute(GentlemanSignaturejiashizhengshibieDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ocr/ocrDrivingLic", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturejiashizhengshibieDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        list.add(new RuleElementVo().setName("初次领证日期").setInfo("初次领证日期").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("性别").setInfo("性别").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("证号").setInfo("证号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("至").setInfo("至").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("住址").setInfo("住址").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("有效期限").setInfo("有效期限").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("准驾车型").setInfo("准驾车型").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("国籍").setInfo("国籍").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("出生日期").setInfo("出生日期").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("发证单位").setInfo("发证单位").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("档案编号").setInfo("档案编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("记录").setInfo("记录").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        return list;
    }
}
