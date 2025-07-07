//package cn.bctools.rule.tools.network;
//
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import cn.hutool.json.JSONUtil;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * @author guojing
// */
//@Slf4j
//@Rule(value = "网络检测",
//        group = RuleGroup.工具插件,
//        test = true,
//        returnType = ClassType.数组,
//        testShowEnum = TestShowEnum.JSON,
//        order = 50,
////        iconUrl = "rule-linkwan",
//        explain = "检测网络IP或请求url地址是否通畅能访问，通畅返回true。"
//)
//public class NetworkMonitoringServiceImpl implements BaseCustomFunctionInterface<NetworkMonitoringDto> {
//
//    @Override
//    public Object execute(NetworkMonitoringDto dto, Map<String, Object> params) {
//        //判断是否有端口号,判断是否有IP地址
//        Map<String, Boolean> collect = dto.getIps().stream().collect(Collectors.toMap(e -> e, NetWorkUtils::monitoring));
//        log.info("IP检测结果为：{}", JSONUtil.toJsonStr(collect));
//        //表示有未通过的
//        return collect.values().stream().allMatch(e -> e);
//    }
//
//}
