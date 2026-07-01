package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureshujuqianfaqiDto;
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
 * The type Gentleman signatureshujuqianfaqi.
 *
 * @author jvs
 */
@Rule(value = "数据签发起",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "针对一些特殊场景签约服务，君子签提供数据签发起服务，\n" +
                "\n" +
                "1、需要先把原始文件的sha512值计算出来\n" +
                "\n" +
                "2、接口对签署人为个人时，不会校验姓名+身份证号的真实性，需要开发者确保真实性；君子签可单独提供个人身份证二要素的校验接口，需要联系商务充值后才能使用。\n" +
                "\n" +
                "3、接口中签署人信息（证件号）不能重复，最大签署方30个。\n" +
                "\n" +
                "4、该调用接口成功后返回presId（备案号）和seqNo（数据签编号），开发者需要保存返回信息，以便后续接口调用。“"
)
@AllArgsConstructor
public class GentlemanSignatureshujuqianfaqi implements BaseCustomFunctionInterface<GentlemanSignatureshujuqianfaqiDto> {

    @Override
    public Object execute(GentlemanSignatureshujuqianfaqiDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/byteSign", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureshujuqianfaqiDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo presId = new RuleElementVo().setName("presId").setInfo("备案号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(presId);
        RuleElementVo seqNo = new RuleElementVo().setName("seqNo").setInfo("数据签编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(seqNo);
        return list;
    }
}
