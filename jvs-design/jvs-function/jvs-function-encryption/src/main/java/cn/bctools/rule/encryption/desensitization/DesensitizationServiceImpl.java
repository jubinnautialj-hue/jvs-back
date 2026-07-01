package cn.bctools.rule.encryption.desensitization;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.util.DesensitizedUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author bcootls.cn
 */
@Service
@AllArgsConstructor
@Rule(value = "数据脱敏",
        group = RuleGroup.工具插件,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 12,
//        iconUrl = "rule-tuominguize",
        explain = "对数据进行脱敏后返回."
)
public class DesensitizationServiceImpl implements BaseCustomFunctionInterface<DesensitizationDto> {

    @Override
    public Object execute(DesensitizationDto dto, Map<String, Object> params) {
        if (dto.getBody() instanceof Map) {
            Map body = (Map) dto.getBody();
            extracted(dto, body);
            return body;
        }
        if (dto.getBody() instanceof List) {
            //兼容两种数据格式
            return ((List<?>) dto.getBody()).stream().peek(e -> extracted(dto, (Map) e)).collect(Collectors.toList());
        }
        return dto.getBody();
    }

    private static void extracted(DesensitizationDto dto, Map body) {
        //获取的数据源
        for (String key : dto.getMap().keySet()) {
            try {
                String desensitized = DesensitizedUtil.desensitized(body.get(key).toString(), dto.getMap().get(key));
                //处理脱敏
                body.put(key, desensitized);
            } catch (Exception ignored) {
            }
        }
    }

}
