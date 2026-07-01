package cn.bctools.rule.tools.file;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import cn.bctools.rule.entity.enums.type.OutputType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zxk
 */
@Service
public class FileOutputTypeSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(OutputType.values())
                .map(e -> new ParameterOption<String>().setLabel(e.msg).setValue(e.name()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }

}
