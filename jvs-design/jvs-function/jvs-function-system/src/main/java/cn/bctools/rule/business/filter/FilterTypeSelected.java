package cn.bctools.rule.business.filter;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import cn.bctools.rule.entity.enums.FilterType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Slf4j
@Service
@AllArgsConstructor
public class FilterTypeSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(FilterType.values()).map(e -> new ParameterOption<String>().setLabel(e.getDesc()).setValue(e.name())).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        return FilterType.AND.name();
    }
}
