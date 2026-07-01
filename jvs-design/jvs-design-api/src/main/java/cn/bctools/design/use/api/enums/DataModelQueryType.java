package cn.bctools.design.use.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * @author jvs
 * The enum Data model query type.
 */
@Getter
@AllArgsConstructor
public enum DataModelQueryType {

    /**
     * 精确查询
     */
    eq("等于"),
    /**
     * Is null data model query type.
     */
    isNull("等于空"),
    /**
     * Ne data model query type.
     */
    ne("不等于"),
    /**
     * Gt data model query type.
     */
    gt("大于"),
    /**
     * Ge data model query type.
     */
    ge("大于等于"),
    /**
     * Lt data model query type.
     */
    lt("小于"),
    /**
     * Le data model query type.
     */
    le("小于等于"),
    /**
     * In data model query type.
     */
    in("包含"),
    /**
     * Allin data model query type.
     */
    allin("全包含"),
    /**
     * Not in data model query type.
     */
    notIn("不包含"),
    /**
     * Like data model query type.
     */
    like("模糊匹配"),
    /**
     * Between data model query type.
     */
    between("之间"),
    ;

    private final String desc;
}
