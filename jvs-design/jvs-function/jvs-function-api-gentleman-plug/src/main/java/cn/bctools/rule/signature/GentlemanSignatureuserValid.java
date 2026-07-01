package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureuserValidDto;
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
 * The type Gentleman signatureuser valid.
 * @author jvs
 */
@Rule(value = "二要素校验",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供个人二要素校验，该接口主要通过姓名+身份证号进行校验。该接口涉及到身份认证费用，如果未购买需要联系商务进行购买。”"
)
@AllArgsConstructor
public class GentlemanSignatureuserValid implements BaseCustomFunctionInterface<GentlemanSignatureuserValidDto> {

    @Override

    public Object execute(GentlemanSignatureuserValidDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/userValid", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureuserValidDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo message = new RuleElementVo().setName("message").setInfo("消息").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(message);
        RuleElementVo valid = new RuleElementVo().setName("valid").setInfo("验证成功与否").setJvsParamType(JvsParamType.bool).setJvsParamTypeName(JvsParamType.bool.getDesc());
        list.add(valid);
        RuleElementVo seqNo = new RuleElementVo().setName("seqNo").setInfo("编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(seqNo);
        RuleElementVo code = new RuleElementVo().setName("code").setInfo("状态码").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(code);
        return list;
    }
}
