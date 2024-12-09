package cn.bctools.rule.entity.enums;

import cn.bctools.rule.entity.enums.type.RuleFile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author guojing
 * @describe 数据类型枚举
 */
@Getter
@AllArgsConstructor
public enum ClassType {
    /**
     * 数据支持的类型操作符
     */
    文本(String.class),
    /**
     * 数据支持的类型操作符
     */
    时间(Date.class),
    /**
     * 数据支持的类型操作符
     */
    数字(Number.class),
    /**
     * 数据支持的类型操作符
     */
    浮点数(Number.class),
    /**
     * 数据支持的类型操作符
     */
    布尔(Boolean.class),
    /**
     * 数据支持的类型操作符
     * 只有集合类型才允许被结构解析，其它 基础类型默认不给予解析功能
     */
    数组(List.class),
    /**
     * 数据支持的类型操作符
     * 只有对象结构的，才能进行自定义结构解析
     */
    对象(Map.class),
    /**
     * 文件链接，用于生成类型的数据处理,和数据返回格式处理
     */
    文件(RuleFile.class),
    /**
     * 数据支持的类型操作符，未识别为接口形式的参数未处理,某些节点，只有处理没有返回， 或返回为空时，在整个逻辑处理过程中无法进行选中,只有在有限数据对象中才能使用结果
     */
    未识别(null),
    ;

    private final Class<?> aClass;

    public static ClassType value(Class<?> aClass) {
        for (ClassType value : ClassType.values()) {
            if (aClass.equals(value.aClass)) {
                return value;
            }
        }
        return null;
    }

    public static ClassType getType(Object obj) {
        for (ClassType value : ClassType.values()) {
            if (obj.getClass().equals(value.aClass)) {
                return value;
            }
        }
        return ClassType.未识别;
    }

}
