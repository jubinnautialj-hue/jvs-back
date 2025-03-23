package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignaturezhuijiaqianshufangDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signaturezhuijiaqianshufang.
 *
 * @author jvs
 */
@Rule(value = "签约发起追加签署人",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "平台业务场景中，在发起合同时存在多个签署人时，不知某个/某几个签署人信息时，可以通过该接口实现追加签署人。\n" +
                "\n" +
                "注意：\n" +
                "\n" +
                "1、需要先调合同发起接口（$SERVICE_URL/v2/sign/applySign），且设置isArchive=0（不归档），才能调该接口继续追加签署人\n" +
                "\n" +
                "2、调本接口时如果设置isArchive=1时，且添加签署人/不添加签署人，对合同进行归档，归档成功后则不能再调该接口\n" +
                "\n" +
                "3、调本接口时如果设置isArchive=0时，合同未归档，则可以继续调该接口追加签署人；"
)
@AllArgsConstructor
public class GentlemanSignaturezhuijiaqianshufang implements BaseCustomFunctionInterface<GentlemanSignaturezhuijiaqianshufangDto> {

    @Override
    public Object execute(GentlemanSignaturezhuijiaqianshufangDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/signatory/add", dto);
    }

}
