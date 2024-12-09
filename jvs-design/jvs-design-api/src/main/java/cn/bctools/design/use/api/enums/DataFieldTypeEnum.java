package cn.bctools.design.use.api.enums;

import cn.bctools.common.utils.ObjectNull;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 字段类型，数据模型的字段类型
 *
 * @author guojing
 */
@Getter
public enum DataFieldTypeEnum {

    /**
     * 数据支持的类型操作符
     */
    字符串(String.class),
    /**
     * 数据支持的类型操作符
     */
    时间(String.class),
    /**
     * 数据支持的类型操作符
     */
    数字(Number.class),
    /**
     * 数据支持的类型操作符
     */
    布尔(Boolean.class),
    /**
     * 数据支持的类型操作符
     * 只有集合类型才允许被结构解析，其它 基础类型默认不给予解析功能
     */
    集合类型(List.class),
    /**
     * 数据支持的类型操作符
     * 只有对象结构的，才能进行自定义结构解析
     */
    未识别(null),

    对象(Map.class);

    private final Class<?> aClass;


    DataFieldTypeEnum(Class<?> aClass) {
        this.aClass = aClass;
    }

    public static DataFieldTypeEnum value(Class<?> aClass) {
        if (ObjectNull.isNotNull(aClass)) {
            for (DataFieldTypeEnum value : DataFieldTypeEnum.values()) {
                if (aClass.equals(value.aClass)) {
                    return value;
                }
            }
        }
        return DataFieldTypeEnum.字符串;
    }

    public static DataFieldTypeEnum value(String name) {
        for (DataFieldTypeEnum value : DataFieldTypeEnum.values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return DataFieldTypeEnum.未识别;
    }

    public static DataFieldTypeEnum getType(Object obj) {
        for (DataFieldTypeEnum value : DataFieldTypeEnum.values()) {
            if (obj.getClass().equals(value.aClass)) {
                return value;
            }
        }
        return DataFieldTypeEnum.未识别;
    }

}
