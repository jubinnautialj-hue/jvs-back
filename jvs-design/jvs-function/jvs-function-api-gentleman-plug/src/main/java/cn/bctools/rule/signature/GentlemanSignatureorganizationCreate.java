package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignatureorganizationCreateDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signatureorganization create.
 *
 * @author jvs
 */
@Rule(value = "企业实名认证上传",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "参与签约的企业用户，需事先通过实名认证。该接口提供企业基本工商三要素认证能力，认证通过后，才能对企业发起合同签署；有涉及到签约的企业用户此接口必调。\n" +
                "\n" +
                "1、接口不对传入数据的来源核实，需要开发者确保传入数据的合法性、准确及真实性。\n" +
                "\n" +
                "2、接口只对企业工商基本三要素（企业名称+营业执照号+法人姓名）做异步校验，不对企业真实意愿（法人/授权人人脸）做校验，需要开发者保证企业使用电子签章的真实意愿，开发者可以调用君子签提供的个人验证服务来确保企业真实意愿。")
@AllArgsConstructor
public class GentlemanSignatureorganizationCreate implements BaseCustomFunctionInterface<GentlemanSignatureorganizationCreateDto> {

    @Override
    public Object execute(GentlemanSignatureorganizationCreateDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/organizationCreate", dto);
    }

}
