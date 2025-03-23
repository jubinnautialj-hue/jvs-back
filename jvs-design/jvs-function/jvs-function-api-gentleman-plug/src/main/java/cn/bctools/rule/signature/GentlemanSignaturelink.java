package cn.bctools.rule.signature;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.dto.GentlemanSignaturelinkDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * The type Gentleman signaturelink.
 *
 * @author jvs
 */
@Rule(value = "获取签约链接",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "交互签署模式需要在发起签约合同后，通过本接口取到签约的url，并转至用户签约页面去浏览合同完成合同签署动作。\n" +
                "\n" +
                "1、签约链接地址有效期为72小时，超过72小时候需要重新获取签约链接地址。\n" +
                "\n" +
                "2、通过签约链接地址签署完成合同后默认是跳转到合同查看详情页面，如果跳转到自己的业务系统中，可以在签约链接地址后面加上跳转地址，格式为&backUrl=跳转地址（跳转地址需经过UrlEncode编码）。用户签署完成后立刻跳转到接入方指定页面上。跳转后会加入两个参数:targetUrl+\"signNo=\"+signNo+\"&status=1\"signNo:签约流水号status：签约状态（1，已签；2，拒签）。平台可根据业务需要决定是否选用跳转功能"
)
@AllArgsConstructor
public class GentlemanSignaturelink implements BaseCustomFunctionInterface<GentlemanSignaturelinkDto> {

    @Override
    public Object execute(GentlemanSignaturelinkDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/link", dto);
    }

}
