package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignatureapplyArbitrationDto;

import java.util.Map;

/**
 * The type Gentleman signatureapply arbitration.
 *
 * @author jvs
 */
@Rule(value = "申请仲裁",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供仲裁申请服务。该接口涉及到仲裁服务费用，如果未购买需要联系商务进行购买。\n" +
                "\n" +
                "注：1、该接口中涉及到需要上传文件材料或者证件照信息等资料，需要先调上传文件的接口（$SERVICE_URL/v2/file/upload），获取到文件路径再赋值到对应参数中。"
)
@AllArgsConstructor
public class GentlemanSignatureapplyArbitration implements BaseCustomFunctionInterface<GentlemanSignatureapplyArbitrationDto> {

    @Override
    public Object execute(GentlemanSignatureapplyArbitrationDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ebqarb/apply", dto);
    }

}
