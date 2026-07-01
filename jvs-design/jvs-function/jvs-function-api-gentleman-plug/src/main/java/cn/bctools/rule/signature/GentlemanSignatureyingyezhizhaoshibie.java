package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureyingyezhizhaoshibieDto;
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
 * The type Gentleman signatureyingyezhizhaoshibie.
 *
 * @author jvs
 */
@Rule(value = "君子签营业执照识别",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供OCR营业执照识别，通过上传营业执照照片进行识别。该接口涉及到OCR识别费用，如果未购买需要联系商务进行购买。"
)
@AllArgsConstructor
public class GentlemanSignatureyingyezhizhaoshibie implements BaseCustomFunctionInterface<GentlemanSignatureyingyezhizhaoshibieDto> {

    @Override
    public Object execute(GentlemanSignatureyingyezhizhaoshibieDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ocr/ocrBusinessLic", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureyingyezhizhaoshibieDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo address = new RuleElementVo().setName("address").setInfo("地址（住所）").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(address);
        RuleElementVo creditCode = new RuleElementVo().setName("creditCode").setInfo("统一社会信用代码").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(creditCode);
        RuleElementVo identityNo = new RuleElementVo().setName("identityNo").setInfo("证照编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(identityNo);
        RuleElementVo legalPerson = new RuleElementVo().setName("legalPerson").setInfo("法定代表人姓名").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(legalPerson);
        RuleElementVo name = new RuleElementVo().setName("name").setInfo("企业名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(name);
        RuleElementVo validity = new RuleElementVo().setName("validity").setInfo("有效期").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(validity);
        return list;
    }
}
