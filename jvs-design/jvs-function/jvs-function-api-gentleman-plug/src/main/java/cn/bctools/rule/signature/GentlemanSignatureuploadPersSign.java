package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignatureuploadPersSignDto;

import java.util.Map;

/**
 * The type Gentleman signatureupload pers sign.
 *
 * @author jvs
 */
@Rule(value = "上传个人自定义签名图片",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "上传个人自定义手写签名图片或者是个人公章图片，可以用于签署合同。此接口非必调，个人可以在签署合同的时候在线手写签名或者是使用君子签系统自动生成的标准印章完成合同签署。接口不会对上传的签名图片做真实性校验，需要开发者确保真实性。“"
)
@AllArgsConstructor
public class GentlemanSignatureuploadPersSign implements BaseCustomFunctionInterface<GentlemanSignatureuploadPersSignDto> {

    @Override
    public Object execute(GentlemanSignatureuploadPersSignDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/uploadPersSign", dto);
    }

}
