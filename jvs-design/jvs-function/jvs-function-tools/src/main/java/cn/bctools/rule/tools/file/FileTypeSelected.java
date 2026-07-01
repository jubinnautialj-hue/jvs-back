package cn.bctools.rule.tools.file;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author st
 */
@Service
public class FileTypeSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(FileType.values())
                .map(e -> new ParameterOption<String>().setLabel(e.name()).setValue(e.name()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }

}
