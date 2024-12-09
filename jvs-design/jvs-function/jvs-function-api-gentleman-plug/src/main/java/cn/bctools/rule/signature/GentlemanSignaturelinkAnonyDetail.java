package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignaturelinkAnonyDetailDto;

import java.util.Map;

/**
 * The type Gentleman signaturelink anony detail.
 *
 * @author jvs
 */
@Rule(value = "获取签约查看链接",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过该接口获取签约完成后的合同查看链接地址。链接地址有效期是7天，超过7天后需要重新获取连接地址"
)
@AllArgsConstructor
public class GentlemanSignaturelinkAnonyDetail implements BaseCustomFunctionInterface<GentlemanSignaturelinkAnonyDetailDto> {

    @Override
    public Object execute(GentlemanSignaturelinkAnonyDetailDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/linkAnonyDetail", dto);
    }

}
