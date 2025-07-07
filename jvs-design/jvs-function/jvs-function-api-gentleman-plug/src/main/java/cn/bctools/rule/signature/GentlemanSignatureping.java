//package cn.bctools.rule.signature;
//
//import cn.bctools.common.utils.PasswordUtil;
//import cn.bctools.common.utils.SpringContextUtil;
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.dto.JunZiQianBaseDto;
//import cn.bctools.rule.dto.JunZiQianOption;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import com.alibaba.fastjson2.JSONObject;
//import com.junziqian.sdk.util.RequestUtils;
//import cn.bctools.rule.utils.JunZiQianUtils;
//import lombok.AllArgsConstructor;
//
//import java.util.Map;
//
//@Rule(value = "测试帐号",
//        group = RuleGroup.君子签,
//        test = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 2,
//        explain = "PING服务-用于测试服务的连通性，确定服务是否可用"
//)
//@AllArgsConstructor
//public class GentlemanSignatureping implements BaseCustomFunctionInterface<JunZiQianBaseDto> {
//
//    @Override
//    public Object execute(JunZiQianBaseDto dto, Map<String, Object> params) {
//        JunZiQianOption junZiQianOption = JSONObject.parseObject(PasswordUtil.decodedPassword(dto.getOptions(), SpringContextUtil.getKey()), JunZiQianOption.class);
//        return RequestUtils.init(junZiQianOption.getServiceUrl(), junZiQianOption.getAppKey(), junZiQianOption.getAppSecret()).doPost("/v2/ping").isSuccess();
//    }
//
//}
