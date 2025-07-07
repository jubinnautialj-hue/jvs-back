//package cn.bctools.rule.signature;
//
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import cn.bctools.rule.utils.JunZiQianUtils;
//import lombok.AllArgsConstructor;
//import cn.bctools.rule.dto.GentlemanSignatureapplySignPosiTypeDto;
//
//import java.util.Map;
//
//@Rule(value = "签字位置指定",
//        group = RuleGroup.君子签,
//        test = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 2,
//        explain = "设定签约方的chapteJson来指定签字位置，其chateJSON是一个JSONArray数组，其结构如下\n" +
//                "\n" +
//                "1、使用签字坐标位置（X Y）需要设置参数positionType=0；且合同发起接口传入签署方信息（signatories）中，需要设置参数chapteJson；\n" +
//                "\n" +
//                "2、注意数组中不能有相同的page页，即不能有相同的第n页。如果有相同的页的不同位置，建议合到一个JSONObject中指定不同的chates;\n" +
//                "\n" +
//                "3、page是从0开始，即page=0表示文件的第一页;"
//)
//@AllArgsConstructor
//public class GentlemanSignatureapplySignPosiType implements BaseCustomFunctionInterface<GentlemanSignatureapplySignPosiTypeDto> {
//
//    @Override
//    public Object execute(GentlemanSignatureapplySignPosiTypeDto dto, Map<String, Object> params) {
//        return JunZiQianUtils.post("", dto);
//    }
//
//}
