package cn.bctools.design.data.fields.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <查询类型>
 *
 * @author auto
 **/
@Getter
@AllArgsConstructor
public enum QueryTypeEnum {

    /**
     * 查询类型
     */
    query_like("模糊","%"),
    query_left_like("左模糊","%l"),
    query_right_like("右模糊","%r"),
    query_eq("等于",""),
    query_ne("不等于","<>"),
    query_gt("大于",">"),
    query_ge("大于等于",">="),
    query_lt("小于","<"),
    query_le("小于等于","<="),
    query_in("包含","{}"),
    query_between("范围","::"),
    ;

    private final String description;
    private final String jsqlSuffix;
}
