package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignatureorganizationFaceCreateDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signatureorganization face create.
 *
 * @author jvs
 */
@Rule(value = "企业人脸校验上传",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "企业注册认证接口，该接口提供了认证页面，法人或者代理人人脸识别通过后，在线上传营业执照图片完成企业注册认证。"
)
@AllArgsConstructor
public class GentlemanSignatureorganizationFaceCreate implements BaseCustomFunctionInterface<GentlemanSignatureorganizationFaceCreateDto> {

    @Override
    public Object execute(GentlemanSignatureorganizationFaceCreateDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/organizationFaceCreate", dto);
    }

}
