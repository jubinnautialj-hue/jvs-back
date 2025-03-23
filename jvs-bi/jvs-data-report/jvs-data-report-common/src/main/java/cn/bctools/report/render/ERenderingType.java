package cn.bctools.report.render;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ERenderingType {

    NORMAL(CalculationType.NORMAL),
    CROSS_TAB(CalculationType.CROSS_TAB),
    FUNCTION(CalculationType.FUNCTION),
    ;
    private final String serviceName;
}
