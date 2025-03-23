package cn.bctools.chart.query.impl;

import cn.bctools.chart.query.ChartQueryTypeValue;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
@AllArgsConstructor
@Slf4j
public class ChartQueryTypeValueDeptId implements ChartQueryTypeValue {
    @Override
    public String execute(String value) {
//        return UserCurrentUtils.getDeptId();
        return null;
    }
}
