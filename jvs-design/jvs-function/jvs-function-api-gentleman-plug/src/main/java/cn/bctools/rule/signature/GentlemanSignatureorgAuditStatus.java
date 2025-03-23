package cn.bctools.rule.signature;//package cn.bctools.rule.signature;
//
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import cn.bctools.rule.utils.JunZiQianUtils;
//import lombok.AllArgsConstructor;
//import cn.bctools.rule.dto.GentlemanSignatureorgAuditStatusDto;
//
//import java.util.Map;
//
//@Rule(value = "企业审核回调",
//        group = RuleGroup.君子签,
//        test = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 2,
//        explain = "查看企业审核回调说明之前请仔细看一下“异步回调”说明介绍，企业注册审核成功或驳回时会发送结果到相应的url地址"
//)
//@AllArgsConstructor
//public class GentlemanSignatureorgAuditStatus implements BaseCustomFunctionInterface<GentlemanSignatureorgAuditStatusDto> {
//
//    @Override
//    public Object execute(GentlemanSignatureorgAuditStatusDto dto, Map<String, Object> params) {
//        return JunZiQianUtils.post("", dto);
//    }
//
//}
