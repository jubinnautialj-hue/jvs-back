package cn.bctools.design.data.fields.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表单组件类型
 *
 * @Author: GuoZi
 */
@Getter
@AllArgsConstructor
public enum FormComponentType {

    /**
     * 容器组件
     */
    container("容器组件"),
    /**
     * 基础组件
     */
    basic("基础组件"),
    /**
     * 高级组件
     */
    advance("高级组件"),
    /**
     * 扩展组件
     */
    extension("扩展组件"),
    ;

    private final String desc;

}
