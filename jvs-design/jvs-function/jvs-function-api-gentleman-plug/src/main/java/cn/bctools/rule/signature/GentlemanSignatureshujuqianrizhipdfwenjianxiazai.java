package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureshujuqianrizhipdfwenjianxiazaiDto;
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
 * The type Gentleman signatureshujuqianrizhipdfwenjianxiazai.
 *
 * @author jvs
 */
@Rule(value = "数据签日志PDF文件下载",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "数据签日志pdf文件下载”"
)
@AllArgsConstructor
public class GentlemanSignatureshujuqianrizhipdfwenjianxiazai implements BaseCustomFunctionInterface<GentlemanSignatureshujuqianrizhipdfwenjianxiazaiDto> {

    @Override
    public Object execute(GentlemanSignatureshujuqianrizhipdfwenjianxiazaiDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/byteSignLog", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureshujuqianrizhipdfwenjianxiazaiDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo presId = new RuleElementVo().setName("presId").setInfo("备案号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(presId);
        RuleElementVo seqNo = new RuleElementVo().setName("seqNo").setInfo("数据签编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(seqNo);
        RuleElementVo pdf = new RuleElementVo().setName("pdf").setInfo("数据签日志pdf文件base64编码").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(pdf);
        return list;
    }
}
