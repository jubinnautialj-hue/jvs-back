package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureocrIdentityDto;
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
 * The type Gentleman signatureocr identity.
 *
 * @author jvs
 */
@Rule(value = "君子签身份证识别",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供OCR身份证识别，通过上传身份证照片识别用户姓名和身份证号。该接口涉及到OCR识别费用，如果未购买需要联系商务进行购买。"
)
@AllArgsConstructor
public class GentlemanSignatureocrIdentity implements BaseCustomFunctionInterface<GentlemanSignatureocrIdentityDto> {

    @Override
    public Object execute(GentlemanSignatureocrIdentityDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ocr/ocrIdentity", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureocrIdentityDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo address = new RuleElementVo().setName("address").setInfo("户籍地住址(上传人像面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(address);
        RuleElementVo birthday = new RuleElementVo().setName("birthday").setInfo("出生日期(上传人像面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(birthday);
        RuleElementVo gender = new RuleElementVo().setName("gender").setInfo("性别(上传人像面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(gender);
        RuleElementVo idNo = new RuleElementVo().setName("idNo").setInfo("身份证号码 (上传人像面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(idNo);
        RuleElementVo name = new RuleElementVo().setName("name").setInfo("姓名(上传人像面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(name);
        RuleElementVo nation = new RuleElementVo().setName("nation").setInfo("民族(上传人像面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(nation);
        RuleElementVo issuingAuthority = new RuleElementVo().setName("issuingAuthority").setInfo("发证机关(上传国徽面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(issuingAuthority);
        RuleElementVo issuingTime = new RuleElementVo().setName("issuingTime").setInfo("有效期开始时间(上传国徽面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(issuingTime);
        RuleElementVo overdueTime = new RuleElementVo().setName("overdueTime").setInfo("有效期结束时间(上传国徽面时必返)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(overdueTime);
        RuleElementVo orderNo = new RuleElementVo().setName("orderNo").setInfo("流水号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(orderNo);
        RuleElementVo executeStatus = new RuleElementVo().setName("executeStatus").setInfo("执行状态（0:success;1:fail）").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(executeStatus);
        RuleElementVo resultMessage = new RuleElementVo().setName("resultMessage").setInfo("返回信息").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(resultMessage);
        return list;
    }
}
