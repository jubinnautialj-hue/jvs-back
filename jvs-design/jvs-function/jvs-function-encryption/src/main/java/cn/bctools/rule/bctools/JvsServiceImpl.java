package cn.bctools.rule.bctools;

import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@AllArgsConstructor
@Rule(value = "JVS解密",
        group = RuleGroup.常用插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 20,
//        iconUrl = "rule-ipvgateway",
        explain = "数据解析将数据进行解密为明文"

)
public class JvsServiceImpl implements BaseCustomFunctionInterface<JvsFunctionDto> {

    @Override
    public Object execute(JvsFunctionDto dto, Map<String, Object> params) {
        if (dto.getOnOff()) {
            return (PasswordUtil.encodePassword(dto.getBody()));
        }
        String text = PasswordUtil.decodedPassword(dto.getBody().replaceAll("\"", ""));
        boolean validObject = JSON.isValidObject(text);
        if (validObject) {
            return JSON.parseObject(text);
        }
        return text;
    }

}
