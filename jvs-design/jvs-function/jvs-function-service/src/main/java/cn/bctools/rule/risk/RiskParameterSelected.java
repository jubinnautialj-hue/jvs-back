package cn.bctools.rule.risk;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.risk.api.RiskApi;
import cn.bctools.risk.dto.RiskStrategyFlowDto;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class RiskParameterSelected implements ParameterSelected<String> {

    RiskApi riskApi;

    @Override
    public List<ParameterOption<String>> getOptions() {
        try {
            List<RiskStrategyFlowDto> data = riskApi.getProjectFlow().getData();
            if (ObjectNull.isNotNull(data)) {
                return data.stream()
                        .map(e ->
                                new ParameterOption<String>().setLabel(e.getName()).setValue(e.getNo())
                        ).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("规则引擎不可用");
        }
        return new ArrayList<>();
    }


    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }

}
