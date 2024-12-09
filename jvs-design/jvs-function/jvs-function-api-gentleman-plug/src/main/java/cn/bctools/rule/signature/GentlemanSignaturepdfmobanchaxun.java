package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturepdfmobanchaxunDto;
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
 * The type Gentleman signaturepdfmobanchaxun.
 *
 * @author jvs
 */
@Rule(value = "PDF模板查询",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "可查询上传的所有PDF模板"
)
@AllArgsConstructor
public class GentlemanSignaturepdfmobanchaxun implements BaseCustomFunctionInterface<GentlemanSignaturepdfmobanchaxunDto> {

    @Override
    public Object execute(GentlemanSignaturepdfmobanchaxunDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/fileTpl/list", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturepdfmobanchaxunDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo createTime = new RuleElementVo().setName("createTime").setInfo("模板创建时间").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(createTime);
        RuleElementVo downLink = new RuleElementVo().setName("downLink").setInfo("模板下载链接").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(downLink);
        RuleElementVo fileName = new RuleElementVo().setName("fileName").setInfo("模板名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(fileName);
        RuleElementVo templateNo = new RuleElementVo().setName("templateNo").setInfo("模板ID").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(templateNo);
        return list;
    }
}
