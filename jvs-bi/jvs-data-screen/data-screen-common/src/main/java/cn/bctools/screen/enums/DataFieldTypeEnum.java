package cn.bctools.screen.enums;

import cn.bctools.common.utils.ObjectNull;
import lombok.Getter;

import java.util.Date;
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
    时间(Date.class),
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
    对象(Map.class),
    /**
     * 用户信息 这里数据为用户id
     */
    用户个人信息(Object.class),
    /**
     * 部门信息
     */
    部门信息(Object.class),
    /**
     * 角色信息
     */
    角色信息(Object.class),
    /**
     * 岗位信息
     */
    岗位信息(Object.class),
    /**
     * 图片
     */
    图片信息(Map.class),
    /**
     * 字典
     */
    系统字典(Object.class),
    /**
     * 字典
     */
    应用字典(Object.class),
    /**
     * 子表 表示一个子集数据
     */
    子表(Object.class),
    /**
     * 数据支持的类型操作符，未识别为接口形式的参数未处理,某些节点，只有处理没有返回， 或返回为空时，在整个逻辑处理过程中无法进行选中,只有在有限数据对象中才能使用结果
     */
    未识别(null),
    ;

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

    public static DataFieldTypeEnum getType(String obj) {
        for (DataFieldTypeEnum value : DataFieldTypeEnum.values()) {
            if (obj.equals(value.name())) {
                return value;
            }
        }
        return DataFieldTypeEnum.未识别;
    }

}
