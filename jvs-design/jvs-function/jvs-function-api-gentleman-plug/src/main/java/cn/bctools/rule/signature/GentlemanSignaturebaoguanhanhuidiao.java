//package cn.bctools.rule.signature;
//
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import cn.bctools.rule.utils.JunZiQianUtils;
//import lombok.AllArgsConstructor;
//import cn.bctools.rule.dto.GentlemanSignaturebaoguanhanhuidiaoDto;
//
//import java.util.Map;
//
//@Rule(value = "保管函回调",
//        group = RuleGroup.君子签,
//        test = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 2,
//        explain = "查看保管函回调说明之前，请仔细查看一下“异步回调”说明介绍，申请保管函后，会将结果以异步请求url的方案回调。"
//)
//@AllArgsConstructor
//public class GentlemanSignaturebaoguanhanhuidiao implements BaseCustomFunctionInterface<GentlemanSignaturebaoguanhanhuidiaoDto> {
//
//    @Override
//    public Object execute(GentlemanSignaturebaoguanhanhuidiaoDto dto, Map<String, Object> params) {
//        return JunZiQianUtils.post("", dto);
//    }
//
//}
