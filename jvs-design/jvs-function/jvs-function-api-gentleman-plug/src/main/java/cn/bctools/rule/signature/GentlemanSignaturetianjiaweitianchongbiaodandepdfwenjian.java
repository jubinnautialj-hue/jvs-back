package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturetianjiaweitianchongbiaodandepdfwenjianDto;
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
 * The type Gentleman signaturetianjiaweitianchongbiaodandepdfwenjian.
 *
 * @author jvs
 */
@Rule(value = "添加未填充变量的文件模板",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "创建或修改文件模板（PDF或Word），需要在君子签后台设置变量，创建好的模版可以登录君子签企业账号后台查看“"
)
@AllArgsConstructor
public class GentlemanSignaturetianjiaweitianchongbiaodandepdfwenjian implements BaseCustomFunctionInterface<GentlemanSignaturetianjiaweitianchongbiaodandepdfwenjianDto> {

    @Override
    public Object execute(GentlemanSignaturetianjiaweitianchongbiaodandepdfwenjianDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/tmpl/tmplPdfSave", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturetianjiaweitianchongbiaodandepdfwenjianDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo templateName = new RuleElementVo().setName("templateName").setInfo("模板名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(templateName);
        RuleElementVo templateNo = new RuleElementVo().setName("templateNo").setInfo("模板编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(templateNo);
        RuleElementVo url = new RuleElementVo().setName("url").setInfo("模版编辑访问链接").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(url);
        return list;
    }
}
