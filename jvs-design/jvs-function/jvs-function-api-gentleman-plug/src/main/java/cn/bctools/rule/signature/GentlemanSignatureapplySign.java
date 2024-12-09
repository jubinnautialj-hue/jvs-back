package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;
import cn.bctools.rule.dto.GentlemanSignatureapplySignDto;

import java.util.Map;

/**
 * The type Gentleman signatureapply sign.
 *
 * @author jvs
 */
@Rule(value = "本地文件发起",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过发起合同接口可以发起： 半自动签署，手动签署（用户有感知）,自动签署（用户无感知），保全存证，hash保全，批量签署合同等多种合同类型。\n" +
                "\n" +
                "1、签署文件上传目前接口支持PDF文件、word文档、ofd文件、HTML文件、君子签API模板，详细说明可参考“发起方式“说明\n" +
                "\n" +
                "2、接口对签署人为个人时，不会校验姓名+身份证号+手机号的真实性，需要开发者确保真实性；君子签可单独提供个人身份证二要素、运营商三要素等的校验接口，需要联系商务充值后才能使用。\n" +
                "\n" +
                "3、接口中签署人信息（证件号）不能重复，同一个签署人可以设置多个签字位置，可以针对不同签署人分别设置验证方式（人脸，银行卡，短信验证码等）、签署方式（自动签、手动签）等。\n" +
                "\n" +
                "4、该调用接口成功后返回APL开头的合同编号，开发者需要保存该合同编号，以便后续接口调用。\n" +
                "\n" +
                "5、签字位置设置说明：接口支持PDF文件关键字定位，坐标（X,Y）定位，表单域（HTML、君子签后台设置API模板），详细说明可参考“签字位置指定”说明“"
)
@AllArgsConstructor
public class GentlemanSignatureapplySign implements BaseCustomFunctionInterface<GentlemanSignatureapplySignDto> {

    @Override
    public Object execute(GentlemanSignatureapplySignDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/applySign", dto);
    }

}
