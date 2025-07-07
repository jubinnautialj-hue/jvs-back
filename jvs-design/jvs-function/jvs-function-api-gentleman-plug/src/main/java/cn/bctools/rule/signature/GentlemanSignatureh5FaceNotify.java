//package cn.bctools.rule.signature;
//
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import cn.bctools.rule.utils.JunZiQianUtils;
//import lombok.AllArgsConstructor;
//import cn.bctools.rule.dto.GentlemanSignatureh5FaceNotifyDto;
//
//import java.util.Map;
//
//@Rule(value = "H5人脸回调",
//        group = RuleGroup.君子签,
//        test = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 2,
//        explain = "查看h5人脸回调说明之前，请仔细查看一下“异步回调”说明介绍，人脸识别完成后，会将识别的结果以异步请求url的方案回调结果"
//)
//@AllArgsConstructor
//public class GentlemanSignatureh5FaceNotify implements BaseCustomFunctionInterface<GentlemanSignatureh5FaceNotifyDto> {
//
//    @Override
//    public Object execute(GentlemanSignatureh5FaceNotifyDto dto, Map<String, Object> params) {
//        return JunZiQianUtils.post("", dto);
//    }
//
//}
