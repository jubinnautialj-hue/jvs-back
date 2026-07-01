package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureqi4e1achaxunshouquanrenDto;
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
 * The type Gentleman signatureqi 4 e 1 achaxunshouquanren.
 *
 * @author jvs
 */
@Rule(value = "企业查询授权人",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "查询授权人"
)
@AllArgsConstructor
public class GentlemanSignatureqi4e1achaxunshouquanren implements BaseCustomFunctionInterface<GentlemanSignatureqi4e1achaxunshouquanrenDto> {

    @Override
    public Object execute(GentlemanSignatureqi4e1achaxunshouquanrenDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ent/certigier/list", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureqi4e1achaxunshouquanrenDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo authorizeCard = new RuleElementVo().setName("authorizeCard").setInfo("授权人身份证号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(authorizeCard);
        RuleElementVo authorizeMobilePhone = new RuleElementVo().setName("authorizeMobilePhone").setInfo("授权人手机号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(authorizeMobilePhone);
        RuleElementVo authorizeName = new RuleElementVo().setName("authorizeName").setInfo("授权人姓名").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(authorizeName);
        RuleElementVo defaultFlag = new RuleElementVo().setName("defaultFlag").setInfo("授权人标识（0 非默认；1默认）").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(defaultFlag);
        RuleElementVo id = new RuleElementVo().setName("id").setInfo("授权人ID").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(id);
        return list;
    }
}
