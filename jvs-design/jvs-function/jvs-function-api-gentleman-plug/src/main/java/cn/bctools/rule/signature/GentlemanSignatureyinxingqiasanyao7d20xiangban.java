package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureyinxingqiasanyao7d20xiangbanDto;
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
 * The type Gentleman signatureyinxingqiasanyao 7 d 20 xiangban.
 *
 * @author jvs
 */
@Rule(value = "银行卡三要素认证详版",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供个人银行卡三要素校验（详版），该接口主要通过身份证号，姓名及银行卡号进行校验，如果验证不一致，则返回具体验证不通过原因。请注意：每张银行卡每日调用最多5次；不同银行卡同一身份证号最多10次，超了就会报错，超限。该接口涉及到身份认证费用，如果未购买需要联系商务进行购买。"
)
@AllArgsConstructor
public class GentlemanSignatureyinxingqiasanyao7d20xiangban implements BaseCustomFunctionInterface<GentlemanSignatureyinxingqiasanyao7d20xiangbanDto> {

    @Override
    public Object execute(GentlemanSignatureyinxingqiasanyao7d20xiangbanDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/bankThreeVerifyDetail", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureyinxingqiasanyao7d20xiangbanDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo orderNo = new RuleElementVo().setName("orderNo").setInfo("订单号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(orderNo);
        RuleElementVo bankName = new RuleElementVo().setName("bankName").setInfo("银行").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(bankName);
        RuleElementVo verifyStatus = new RuleElementVo().setName("verifyStatus").setInfo("验证结果").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(verifyStatus);
        RuleElementVo excuteStatus = new RuleElementVo().setName("excuteStatus").setInfo("执行结果").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(excuteStatus);
        RuleElementVo resultMessage = new RuleElementVo().setName("resultMessage").setInfo("验证失败原因").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(resultMessage);
        RuleElementVo code = new RuleElementVo().setName("code").setInfo("状态码。10415-银行卡号有误，请检查卡号或换卡重试; 10416-姓名或身份证号有误; 0-验证成功").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(code);
        return list;
    }
}
