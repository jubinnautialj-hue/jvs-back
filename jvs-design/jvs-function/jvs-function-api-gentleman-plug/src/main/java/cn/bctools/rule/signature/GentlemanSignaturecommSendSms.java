package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignaturecommSendSmsDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signaturecomm send sms.
 *
 * @author jvs
 */
@Rule(value = "通用短信发送",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过后台手动申请提交模板id、短信模板、签名、变量,完成后通过该接口指定变量值、手机号、模板id完成短信发送(需要开通短信套餐)"
)
@AllArgsConstructor
public class GentlemanSignaturecommSendSms implements BaseCustomFunctionInterface<GentlemanSignaturecommSendSmsDto> {

    @Override
    public Object execute(GentlemanSignaturecommSendSmsDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/commSendSms", dto);
    }

}
