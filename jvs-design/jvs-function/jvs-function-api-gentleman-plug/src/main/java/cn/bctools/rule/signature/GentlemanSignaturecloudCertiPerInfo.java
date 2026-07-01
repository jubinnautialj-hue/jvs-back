package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignaturecloudCertiPerInfoDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signaturecloud certi per info.
 *
 * @author jvs
 */
@Rule(value = "个人证书申请资料上传",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "个人申请CA证书需上传的资料，接口不对传入的个人信息做真实性校验，需要开发者确保真实性。"
)
@AllArgsConstructor
public class GentlemanSignaturecloudCertiPerInfo implements BaseCustomFunctionInterface<GentlemanSignaturecloudCertiPerInfoDto> {

    @Override
    public Object execute(GentlemanSignaturecloudCertiPerInfoDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/cloudCertiPerInfo", dto);
    }

}
