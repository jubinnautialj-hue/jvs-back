package cn.bctools.rule.hexencode;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.TextEncodeDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "HexEncode",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        order = 10,
//        iconUrl = "rule-hsmjiamifuwu",
        explain = "数值使用0-9以及A-F（或a-f）来表示，其中A-F代表十进制的10到15。这种编码方法常用于表示和传输二进制数据，因为它可以将每个字节（8位）转换为两个十六进制字符（共16种可能的组合），从而使数据更易于阅读和解析"
)
public class HexEncodeServiceImpl implements BaseCustomFunctionInterface<TextEncodeDto> {

    @Override
    public Object execute(TextEncodeDto dto, Map<String, Object> params) {
        return Hex.encodeHexString(dto.getText().getBytes());
    }

}
