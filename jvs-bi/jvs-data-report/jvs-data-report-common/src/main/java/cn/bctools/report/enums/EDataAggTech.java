package cn.bctools.report.enums;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@ApiModel("聚合方式")
@Getter
@AllArgsConstructor
public enum EDataAggTech {

    GROUP("分组"),
    LIST("列表"),
    ;
    private final String desc;
}
