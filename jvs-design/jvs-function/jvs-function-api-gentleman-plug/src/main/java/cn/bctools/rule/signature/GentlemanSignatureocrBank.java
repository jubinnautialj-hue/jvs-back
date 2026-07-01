package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureocrBankDto;
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
 * The type Gentleman signatureocr bank.
 *
 * @author jvs
 */
@Rule(value = "君子签银行卡识别",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供OCR银行卡识别，通过上传银行卡照片识别银行卡号。该接口涉及到ocr银行卡识别费用，如果未购买需要联系商务进行购买。"
)
@AllArgsConstructor
public class GentlemanSignatureocrBank implements BaseCustomFunctionInterface<GentlemanSignatureocrBankDto> {

    @Override
    public Object execute(GentlemanSignatureocrBankDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ocr/ocrBank", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureocrBankDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo cardNo = new RuleElementVo().setName("cardNo").setInfo("银行卡号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(cardNo);
        RuleElementVo bankName = new RuleElementVo().setName("bankName").setInfo("发卡行名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(bankName);
        RuleElementVo orderNo = new RuleElementVo().setName("orderNo").setInfo("流水号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(orderNo);
        RuleElementVo executeStatus = new RuleElementVo().setName("executeStatus").setInfo("执行状态（0:success;1:fail）").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(executeStatus);
        RuleElementVo resultMessage = new RuleElementVo().setName("resultMessage").setInfo("返回信息").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(resultMessage);
        RuleElementVo validDate = new RuleElementVo().setName("validDate").setInfo("银行卡有效期").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(validDate);
        return list;
    }
}
