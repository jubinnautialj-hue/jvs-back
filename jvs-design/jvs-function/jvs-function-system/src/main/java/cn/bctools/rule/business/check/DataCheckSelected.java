package cn.bctools.rule.business.check;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Data check selected.
 *
 * @author jvs
 */
@Service
@AllArgsConstructor
public class DataCheckSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(DataCheckSelectedOption.values())
                .map(e -> new ParameterOption<String>().setLabel(e.name()).setValue(e.name())).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0);
    }
}
