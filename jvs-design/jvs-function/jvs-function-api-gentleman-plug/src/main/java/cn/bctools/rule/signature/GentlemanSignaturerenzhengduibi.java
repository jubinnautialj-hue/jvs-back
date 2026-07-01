package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturerenzhengduibiDto;
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
 * The type Gentleman signaturerenzhengduibi.
 *
 * @author jvs
 */
@Rule(value = "人证对比",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "上传人脸图片+姓名+身份证号与公安库留存的个人证件照信息进匹配。“"
)
@AllArgsConstructor
public class GentlemanSignaturerenzhengduibi implements BaseCustomFunctionInterface<GentlemanSignaturerenzhengduibiDto> {

    @Override
    public Object execute(GentlemanSignaturerenzhengduibiDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/baseservice/faceimgMatchId2factor", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturerenzhengduibiDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo seqNum = new RuleElementVo().setName("seqNum").setInfo("处理序列号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(seqNum);
        RuleElementVo message = new RuleElementVo().setName("message").setInfo("处理消息").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(message);
        RuleElementVo matchResult = new RuleElementVo().setName("matchResult").setInfo("匹配结果1000:匹配一致 1001匹配不一致 1002:未匹配到信息").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(matchResult);
        RuleElementVo matchMessage = new RuleElementVo().setName("matchMessage").setInfo("匹配消息").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(matchMessage);
        RuleElementVo matchScore = new RuleElementVo().setName("matchScore").setInfo("匹配分数0-100").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(matchScore);
        return list;
    }
}
