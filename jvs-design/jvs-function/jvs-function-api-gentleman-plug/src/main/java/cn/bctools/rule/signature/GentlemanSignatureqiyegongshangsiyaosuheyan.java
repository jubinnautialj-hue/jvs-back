package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureqiyegongshangsiyaosuheyanDto;
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
 * The type Gentleman signatureqiyegongshangsiyaosuheyan.
 *
 * @author jvs
 */
@Rule(value = "企业工商四要素核验",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "企业工商四要素校验服务，该接口主要通过 企业名称、营业执照号、法人姓名、法人身份证号进行校验。该接口涉及到校验费用，如果未购买套餐需要联系商务进行购买。“"
)
@AllArgsConstructor
public class GentlemanSignatureqiyegongshangsiyaosuheyan implements BaseCustomFunctionInterface<GentlemanSignatureqiyegongshangsiyaosuheyanDto> {

    @Override
    public Object execute(GentlemanSignatureqiyegongshangsiyaosuheyanDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/entFourVerify", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureqiyegongshangsiyaosuheyanDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo code = new RuleElementVo().setName("code").setInfo("状态码1000，表示接口请求成功（不作为判断是否通过标准）").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(code);
        RuleElementVo message = new RuleElementVo().setName("message").setInfo("验证结果描述").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(message);
        RuleElementVo seqNo = new RuleElementVo().setName("seqNo").setInfo("请求序号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(seqNo);
        RuleElementVo valid = new RuleElementVo().setName("valid").setInfo("true验证一致，false验证不一致 （可通过该返回结果判断企业工商四要素是否验证通过）").setJvsParamType(JvsParamType.bool).setJvsParamTypeName(JvsParamType.bool.getDesc());
        list.add(valid);
        return list;
    }
}
