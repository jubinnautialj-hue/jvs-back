package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturexiaochengxurenlianDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Gentleman signaturexiaochengxurenlian.
 *
 * @author jvs
 */
@Rule(value = "小程序人脸",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供单独的小程序人脸识别服务，该接口主要用于小程序里做人脸识别。该接口涉及到人脸识别费用，如果未购买需要联系商务进行购买。\n" +
                "\n" +
                "1、打开另一个小程序，请参考《微信官方文档》，地址：https://developers.weixin.qq.com/miniprogram/dev/api/navigate/wx.navigateToMiniProgram.html\n" +
                "\n" +
                "2、小程序人脸识别时效性为30分钟，超过30分钟后需重新调接口获取人脸相关参数。\n" +
                "\n" +
                "3、打开另一个小程序中appId、path等相关参数请联系君子签对接客服获取。\n" +
                "\n" +
                "4、打开另一个小程序中的extraData参数，需要传入小程序人脸认证接口返回的faceOrderNo、faceId、backPath（平台自定义地址：人脸完成后跳转的页面地址，可不传，不传回到跳转之前的页面）。\n" +
                "\n" +
                "5、打开另一个小程序示例代码如下图："
)
@AllArgsConstructor
public class GentlemanSignaturexiaochengxurenlian implements BaseCustomFunctionInterface<GentlemanSignaturexiaochengxurenlianDto> {

    @Override
    public Object execute(GentlemanSignaturexiaochengxurenlianDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/startMiniAppFace", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturexiaochengxurenlianDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo faceOrderNo = new RuleElementVo().setName("faceOrderNo").setInfo("人脸订单号由服务器生成，和用户传入的订单号不一样").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(faceOrderNo);
        RuleElementVo h5faceId = new RuleElementVo().setName("h5faceId").setInfo("拉起小程序需要的参数人脸参数").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(h5faceId);
        RuleElementVo userOrderNo = new RuleElementVo().setName("userOrderNo").setInfo("用户参数传入的订单号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(userOrderNo);
        return list;
    }
}
