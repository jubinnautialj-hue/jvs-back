package cn.bctools.rule.data.selected;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import cn.bctools.rule.data.enums.OperationEnum;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author st
 */
@Service
public class OperationTypeSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(OperationEnum.values())
                .map(typeEnum -> new ParameterOption<String>().setLabel(typeEnum.getName()).setValue(typeEnum.name()))
                .collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

}
