package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignatureenterpriseInfoMatchDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signatureenterprise info match.
 *
 * @author jvs
 */
@Rule(value = "企业基本信息核验",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供基本工商信息校验服务，该接口主要通过 企业名称、营业执照号、法人姓名进行校验。该接口涉及到企业基本信息校验费用，如果未购买需要联系商务进行购买。"
)
@AllArgsConstructor
public class GentlemanSignatureenterpriseInfoMatch implements BaseCustomFunctionInterface<GentlemanSignatureenterpriseInfoMatchDto> {

    @Override
    public Object execute(GentlemanSignatureenterpriseInfoMatchDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/enterpriseInfoMatch", dto);
    }

}
