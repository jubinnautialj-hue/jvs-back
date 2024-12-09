package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureduanxinyanzhengDto;
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
 * The type Gentleman signatureduanxinyanzheng.
 *
 * @author jvs
 */
@Rule(value = "短信验证码",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "获取短信验证码接口。"
)
@AllArgsConstructor
public class GentlemanSignatureduanxinyanzheng implements BaseCustomFunctionInterface<GentlemanSignatureduanxinyanzhengDto> {

    @Override
    public Object execute(GentlemanSignatureduanxinyanzhengDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/smsVerify", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureduanxinyanzhengDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo orderNo = new RuleElementVo().setName("orderNo").setInfo("流水号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(orderNo);
        RuleElementVo code = new RuleElementVo().setName("code").setInfo("验证码").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(code);
        return list;
    }
}
