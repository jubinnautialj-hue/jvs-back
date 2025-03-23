package cn.bctools.chart.query.impl;

import cn.bctools.chart.query.ChartQueryTypeValue;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ChartQueryTypeValueUserId implements ChartQueryTypeValue {
    @Override
    public String execute(String value) {
        return UserCurrentUtils.getUserId();
    }
}
