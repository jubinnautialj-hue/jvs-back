package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignatureapimobanshouquanmafaqiDto;

import java.util.Map;

/**
 * The type Gentleman signatureapimobanshouquanmafaqi.
 *
 * @author jvs
 */
@Rule(value = "API模板授权码发起",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "平台使用君子签后台的API模板发起合同签署时，确保模板内容的准确性需要向用户获取模板授权码，在调用接口中需要传入模板的授权码才能成功发起合同。:"
)
@AllArgsConstructor
public class GentlemanSignatureapimobanshouquanmafaqi implements BaseCustomFunctionInterface<GentlemanSignatureapimobanshouquanmafaqiDto> {

    @Override
    public Object execute(GentlemanSignatureapimobanshouquanmafaqiDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/tplAuthCode/applySign", dto);
    }

}
