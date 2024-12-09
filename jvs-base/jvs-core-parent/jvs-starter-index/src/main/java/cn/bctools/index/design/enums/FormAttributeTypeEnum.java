package cn.bctools.index.design.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jvs 表单属性类型
 */
@Getter
@AllArgsConstructor
public enum FormAttributeTypeEnum {

    /**
     * 请使用 String 接收
     */
    select("选择框"),
    /**
     * 请使用 String 接收
     */
    tree("树形选择框"),
    /**
     * 请使用 List<String></String> 接收
     */
    selectMultiple("多项选择框"),
    /**
     * 请使用 String 接收
     */
    radio("单选框"),
    /**
     * Search form attribute type enum.
     */
    search("搜索"),
    /**
     * 请使用 String 接收
     */
    input("输入框"),
    ;

    private final String desc;
}
