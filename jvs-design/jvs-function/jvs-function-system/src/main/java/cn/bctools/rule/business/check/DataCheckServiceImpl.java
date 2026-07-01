package cn.bctools.rule.business.check;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author jvs
 */
@Slf4j
@Order(1)
@Service
@Rule(
        value = "数据校验",
        group = RuleGroup.常用插件,
        returnType = ClassType.对象,
        customStructure = false,
        test = true,
        order = 6,
        explain = "对象数据校验格式是否正确或为空、是否为数字、是否有包含特殊字符串"
)
public class DataCheckServiceImpl implements BaseCustomFunctionInterface<DataCheckDto> {

    @Override
    public Object execute(DataCheckDto dto, Map<String, Object> params) {
        StringBuilder str = new StringBuilder();
        Map<String, Object> body = dto.getBody();
        for (String key : dto.getCheck().keySet()) {
            Object o = body.getOrDefault(key, "");
            DataCheckSelectedOption check = DataCheckSelectedOption.valueOf(dto.getCheck().get(key).toString());
            switch (check) {
                case 是否为空:
                    if (ObjectUtil.isEmpty(o)) {
                        //如果为空，则记录
                        str.append(key).append("数据为空");
                    }
                    break;
                case 是否为数字:
                    if (ObjectUtil.isEmpty(o)) {
                        //如果为空，则记录
                        str.append(key).append("数据为空");
                    }
                    if (!StrUtil.isNumeric(o.toString())) {
                        str.append(key).append("不是数字");
                    }
                    break;
                case 是否包含特殊字符:
                    if (ObjectUtil.isEmpty(o)) {
                        //如果为空，则记录
                        str.append(key).append("数据为空");
                    }
                    if (!StrUtil.containsAny(str, "!@#$%^&*()")) {
                        str.append(key).append("不包含特殊字符");
                    }
                    break;
                default:
            }
        }
        return Dict.create().set("status", ObjectNull.isNull(str.toString())).set("message", str.toString());
    }

    @Override
    public List<RuleElementVo> structureType(DataCheckDto o) {
        List<RuleElementVo> objects = new ArrayList<>();
        objects.add(new RuleElementVo().setInfo("校验结果").setName("status").setJvsParamType(JvsParamType.bool));
        objects.add(new RuleElementVo().setInfo("校验失败消息").setName("message").setJvsParamType(JvsParamType.text));
        return objects;
    }
}
