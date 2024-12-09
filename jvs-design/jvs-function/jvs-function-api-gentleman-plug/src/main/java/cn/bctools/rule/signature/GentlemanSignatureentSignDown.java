package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureentSignDownDto;
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
 * The type Gentleman signatureent sign down.
 *
 * @author jvs
 */
@Rule(value = "下载企业自定义印章",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "下载企业自定义上传的公章图片"
)
@AllArgsConstructor
public class GentlemanSignatureentSignDown implements BaseCustomFunctionInterface<GentlemanSignatureentSignDownDto> {

    @Override
    public Object execute(GentlemanSignatureentSignDownDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/entSignDown", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureentSignDownDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo entSignDate = new RuleElementVo().setName("entSignDate").setInfo("公章文件内容base64String字符串").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(entSignDate);
        return list;
    }
}
