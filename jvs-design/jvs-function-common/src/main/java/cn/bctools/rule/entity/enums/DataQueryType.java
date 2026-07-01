package cn.bctools.rule.entity.enums;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jvs
 */
@Getter
@AllArgsConstructor
public enum DataQueryType {
    /**
     * 精确查询
     */
    eq("等于"),
    isNull("等于空"),
    ne("不等于"),
    gt("大于"),
    ge("大于等于"),
    lt("小于"),
    le("小于等于"),
    in("包含"),
    allin("全包含"),
    notIn("不包含"),
    like("模糊匹配"),
    between("之间"),
    ;

    @ApiModelProperty("描述")
    private final String desc;
}
