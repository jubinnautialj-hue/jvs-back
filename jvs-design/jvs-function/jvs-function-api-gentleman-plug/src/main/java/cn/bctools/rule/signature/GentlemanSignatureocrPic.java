package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureocrPicDto;
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
 * The type Gentleman signatureocr pic.
 *
 * @author jvs
 */
@Rule(value = "手写相似度比对",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过接口提交需要识别的 “ 手写内容图片 ” 和需要与之比对的 “文本内容 ”，返回相似度以及识别文本"
)
@AllArgsConstructor
public class GentlemanSignatureocrPic implements BaseCustomFunctionInterface<GentlemanSignatureocrPicDto> {

    @Override
    public Object execute(GentlemanSignatureocrPicDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ocr/ocrPic", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureocrPicDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo executeStatus = new RuleElementVo().setName("executeStatus").setInfo("咨询状态0 成功 1失败").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(executeStatus);
        RuleElementVo orderNo = new RuleElementVo().setName("orderNo").setInfo("订单号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(orderNo);
        RuleElementVo score = new RuleElementVo().setName("score").setInfo("相似度分数0到1之间的小数").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(score);
        RuleElementVo text = new RuleElementVo().setName("text").setInfo("识别文字").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(text);
        return list;
    }
}
