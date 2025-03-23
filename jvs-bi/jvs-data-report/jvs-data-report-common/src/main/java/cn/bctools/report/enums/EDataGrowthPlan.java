package cn.bctools.report.enums;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel("数据拓展方向")
@Getter
@AllArgsConstructor
public enum EDataGrowthPlan {

    HORIZONTAL("横向拓展"),
    VERTICAL("纵向拓展"),
    NONE("不拓展"),
    CROSS_TAB("交叉拓展")
    ;
    private final String desc;

}
