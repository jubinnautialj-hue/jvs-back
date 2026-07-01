package cn.bctools.rule.dingding.work;


import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Ding msg type selected.
 *
 * @author jvs
 */
@Slf4j
@Service
@AllArgsConstructor
public class DingMsgTypeSelected implements ParameterSelected<String> {
    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(DingMsgType.values()).map(e -> new ParameterOption<String>(e.name(), e.name())).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }

}
