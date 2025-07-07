//package cn.bctools.rule.signature;
//
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import cn.bctools.rule.utils.JunZiQianUtils;
//import lombok.AllArgsConstructor;
//import cn.bctools.rule.dto.GentlemanSignaturesignStatusDto;
//
//import java.util.Map;
//
//@Rule(value = "签约回调",
//        group = RuleGroup.君子签,
//        test = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 2,
//        explain = "签约回调 回调说明 [!NOTE]查看签约回调说明之前先仔细看一下异步回调说明介绍，合同签约完成或保全成功时会发起处理完成的结果到相应的url地址 请求参数说明 参数名 值或说明 是否必填(*是) 请求方式 字段类型 method sign.status * post string version 1.0.0 * post string timestamp 时间戳,毫秒 * post long data 参考data说明 ? post JSONObject sign 签名sign(sha1对前面的参数签名) * post string appkey appkey * post string data说明 data内部结构字段 说明 是否必填(*是) 请求方式 字段类型 applyNo 签约编号 * post string identityType 证件类型 1身份证, 2护照, 3台胞证, 4港澳居民来往内地通行证, 11营业执照, 12统一社会信用代码, 99其他 * post int fullName 名称 * post string identityCard 证件号 * post string optTime 操作时间(毫秒) * post long signStatus 1签约完成2拒签3已保全（1、签约完成是指用户签署文件成功；2、拒签是指用户在签约页面点了拒签按钮，需要进入到签署页面用户手动签署才会出现拒签按钮；3、已保全是指用户完成签署后，君子签会立即自动对用户签署过程的操作以及合同文件信息进行上链存证固化，已保全是合同的最终状态，如果需要下载合同请以已保全状态为准。） * post int "
//)
//@AllArgsConstructor
//public class GentlemanSignaturesignStatus implements BaseCustomFunctionInterface<GentlemanSignaturesignStatusDto> {
//
//    @Override
//    public Object execute(GentlemanSignaturesignStatusDto dto, Map<String, Object> params) {
//        return JunZiQianUtils.post("", dto);
//    }
//}
