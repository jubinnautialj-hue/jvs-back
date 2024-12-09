package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturechaxunqiyeyinzhangDto;
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
 * The type Gentleman signaturechaxunqiyeyinzhang.
 *
 * @author jvs
 */
@Rule(value = "查询企业印章",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "查询企业印章"
)
@AllArgsConstructor
public class GentlemanSignaturechaxunqiyeyinzhang implements BaseCustomFunctionInterface<GentlemanSignaturechaxunqiyeyinzhangDto> {

    @Override
    public Object execute(GentlemanSignaturechaxunqiyeyinzhangDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/listEntSign", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturechaxunqiyeyinzhangDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo signId = new RuleElementVo().setName("signId").setInfo("印章ID").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(signId);
        RuleElementVo signName = new RuleElementVo().setName("signName").setInfo("印章名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(signName);
        return list;
    }
}
