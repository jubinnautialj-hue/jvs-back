package cn.bctools.chart.query.impl;

import cn.bctools.chart.query.ChartQueryTypeValue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ChartQueryTypeValueInput implements ChartQueryTypeValue {
    @Override
    public String execute(String value) {
        return value;
    }
}
