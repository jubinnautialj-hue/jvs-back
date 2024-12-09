package cn.bctools.rule.encryption.desensitization;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 选择数据脱敏规则
 *
 * @author bctools.cn
 */
@Slf4j
@Service
@AllArgsConstructor
public class DesensitizationSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(DesensitizationSelectedOption.values())
                .map(e -> new ParameterOption<String>().setLabel(e.msg).setValue(e.name())).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

}
