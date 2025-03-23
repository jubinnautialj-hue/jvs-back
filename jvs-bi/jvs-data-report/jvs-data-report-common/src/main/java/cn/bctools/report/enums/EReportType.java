package cn.bctools.report.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EReportType {

    BASE("BASE","基础报表"),
    BRANCH("BRANCH","分行报表"),
    GROUP("GROUP","分组报表"),
    CROSS_TAB("CROSS_TAB","交叉报表"),
    MULTI_SOURCE("MULTI_SOURCE","多数据源报表"),
    MAIN_SOURCE("MAIN_SOURCE","主子表"),
    ;

    @EnumValue
    private final String code;

    private final String desc;
}
