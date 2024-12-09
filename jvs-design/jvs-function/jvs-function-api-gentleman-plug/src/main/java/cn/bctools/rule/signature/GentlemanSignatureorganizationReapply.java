package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignatureorganizationReapplyDto;

import java.util.Map;

/**
 * The type Gentleman signatureorganization reapply.
 *
 * @author jvs
 */
@Rule(value = "企业实名认证重传",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "提交的企业信息审核不通过的情况下，修改后可以调用此接口重新提交。"
)
@AllArgsConstructor
public class GentlemanSignatureorganizationReapply implements BaseCustomFunctionInterface<GentlemanSignatureorganizationReapplyDto> {

    @Override
    public Object execute(GentlemanSignatureorganizationReapplyDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/organizationReapply", dto);
    }

}
