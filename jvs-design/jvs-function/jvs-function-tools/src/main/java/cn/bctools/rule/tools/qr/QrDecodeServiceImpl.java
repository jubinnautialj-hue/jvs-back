package cn.bctools.rule.tools.qr;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.http.HttpUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * @author zx
 * @describe 二维码生成器
 */
@Rule(value = "二维码解析",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.文本,
        testShowEnum = TestShowEnum.IMG,
        order = 28,
//        iconUrl = "rule-config",
        explain = "根据业务数据生成二维码,生成二维码数据"
)
@AllArgsConstructor
public class QrDecodeServiceImpl implements BaseCustomFunctionInterface<QrDecodeDto> {

    @Override
    @SneakyThrows
    public Object execute(QrDecodeDto dto, Map<String, Object> params) {
        return QrCodeUtil.decode(new ByteArrayInputStream(HttpUtil.downloadBytes(dto.getUrl())));
    }

}
