package cn.bctools.design.data.source.impl.sql.enums;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The enum Dynamic keyword.
 *
 * @Author: ZhuXiaoKang
 * @Description: 动态数据关键字
 */
@Getter
@AllArgsConstructor
public enum DynamicKeyword {

    /**
     * And dynamic keyword.
     */
    AND("且"),
    /**
     * Or dynamic keyword.
     */
    OR("或"),
    /**
     * Eq dynamic keyword.
     */
    EQ("等于"),
    /**
     * Isnull dynamic keyword.
     */
    ISNULL("等于空"),
    /**
     * Ne dynamic keyword.
     */
    NE("不等于"),
    /**
     * Gt dynamic keyword.
     */
    GT("大于"),
    /**
     * Gte dynamic keyword.
     */
    GTE("大于等于"),
    /**
     * Lt dynamic keyword.
     */
    LT("小于"),
    /**
     * Lte dynamic keyword.
     */
    LTE("小于等于"),
    /**
     * In dynamic keyword.
     */
    IN("包含"),
    /**
     * Notin dynamic keyword.
     */
    NOTIN("不包含"),
    /**
     * Like dynamic keyword.
     */
    LIKE("模糊匹配"),
    /**
     * Json like dynamic keyword.
     */
    JSON_LIKE("JSON字段模糊匹配"),
    /**
     * Between dynamic keyword.
     */
    BETWEEN("之间"),
    ;

    @ApiModelProperty("描述")
    private final String desc;
}
