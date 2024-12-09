package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturexingshizhengshibieDto;
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
 * The type Gentleman signaturexingshizhengshibie.
 *
 * @author jvs
 */
@Rule(value = "君子签行驶证识别",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供OCR行驶证识别，通过上传行驶证照片进行识别。该接口涉及到OCR识别费用，如果未购买需要联系商务进行购买。"
)
@AllArgsConstructor
public class GentlemanSignaturexingshizhengshibie implements BaseCustomFunctionInterface<GentlemanSignaturexingshizhengshibieDto> {

    @Override
    public Object execute(GentlemanSignaturexingshizhengshibieDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ocr/ocrVehicleLic", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturexingshizhengshibieDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        list.add(new RuleElementVo().setName("发动机号码").setInfo("发动机号码").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("号牌号码").setInfo("号牌号码").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("注册日期").setInfo("注册日期").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("住址").setInfo("住址").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("发证日期").setInfo("发证日期").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("车辆识别代号").setInfo("车辆识别代号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("所有人").setInfo("所有人").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("品牌型号").setInfo("品牌型号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("发证单位").setInfo("发证单位").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("车辆类型").setInfo("车辆类型").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("使用性质").setInfo("使用性质").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("备注").setInfo("备注").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("整备质量").setInfo("整备质量").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("外廓尺寸").setInfo("外廓尺寸").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("核定载人数").setInfo("核定载人数").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("检验记录").setInfo("检验记录").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("燃油类型").setInfo("燃油类型").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("总质量").setInfo("总质量").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("证芯编号").setInfo("证芯编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("档案编号").setInfo("档案编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("核定载质量").setInfo("核定载质量").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        list.add(new RuleElementVo().setName("准牵引总质量").setInfo("准牵引总质量").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        return list;
    }

}
