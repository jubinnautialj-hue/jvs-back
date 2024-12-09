package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignatureuploadEntSignDto;

import java.util.Map;

/**
 * The type Gentleman signatureupload ent sign.
 *
 * @author jvs
 */
@Rule(value = "上传企业自定义印章",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "上传企业自定义的公章，可用于合同签署。上传公章规格要求：180*180PX，背景透明，png格式。 若不调此接口，合同上盖的企业章默认使用系统自动根据企业名称生成圆形的图形章。”"
)
@AllArgsConstructor
public class GentlemanSignatureuploadEntSign implements BaseCustomFunctionInterface<GentlemanSignatureuploadEntSignDto> {

    @Override
    public Object execute(GentlemanSignatureuploadEntSignDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/uploadEntSign", dto);
    }

}
