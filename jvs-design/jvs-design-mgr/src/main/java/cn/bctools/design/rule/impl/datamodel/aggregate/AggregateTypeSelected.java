package cn.bctools.design.rule.impl.datamodel.aggregate;

import cn.bctools.design.data.fields.enums.AggregateEnumType;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 *  @author jvs
 */
@Slf4j
@Service
@AllArgsConstructor
public class AggregateTypeSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.asList(
                new ParameterOption<String>().setLabel("计数").setValue(AggregateEnumType.count.name()),
                new ParameterOption<String>().setLabel("平均").setValue(AggregateEnumType.ave.name()),
                new ParameterOption<String>().setLabel("最大").setValue(AggregateEnumType.max.name()),
                new ParameterOption<String>().setLabel("最小").setValue(AggregateEnumType.min.name()),
                new ParameterOption<String>().setLabel("求和").setValue(AggregateEnumType.sum.name())
        );
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(1).getValue();
    }


}
