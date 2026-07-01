package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureoperatorsThreeVerifyDto;
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
 * The type Gentleman signatureoperators three verify.
 *
 * @author jvs
 */
@Rule(value = "运营商3要素认证",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供运营商三要素认证服务，该接口通过身份证号、名称、手机号校验是否真实有效（通过查询电信运营商服务接口确认，有效性存在时延）、该接口涉及到运营商三要素费用，如果未购买需要联系商务进行购买。\n" +
                "\n" +
                "注：虚拟号段不支持验证，一卡多号（虚拟卡）不支持验证，192中国广电号段不支持验证"
)
@AllArgsConstructor
public class GentlemanSignatureoperatorsThreeVerify implements BaseCustomFunctionInterface<GentlemanSignatureoperatorsThreeVerifyDto> {

    @Override
    public Object execute(GentlemanSignatureoperatorsThreeVerifyDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/operatorsThreeVerify", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureoperatorsThreeVerifyDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo orderNo = new RuleElementVo().setName("orderNo").setInfo("订单号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(orderNo);
        RuleElementVo code = new RuleElementVo().setName("code").setInfo("0成功 ，2失败").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(code);
        RuleElementVo verifyStatus = new RuleElementVo().setName("verifyStatus").setInfo("验证结果").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(verifyStatus);
        RuleElementVo excuteStatus = new RuleElementVo().setName("excuteStatus").setInfo("执行结果").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(excuteStatus);
        RuleElementVo resultMessage = new RuleElementVo().setName("resultMessage").setInfo("验证失败原因").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(resultMessage);
        return list;
    }
}
