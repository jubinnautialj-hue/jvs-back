package cn.bctools.index.design;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 组件的属性值有哪些是可选的值
 *
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class SelectedAttribute {

    /**
     * 类型,用于后端设计和获取数据区分,标识, 同一个组件,可以有多个不同的组件来标识. 后端可分开处理不同数据逻辑
     */
    String type;
    /**
     * 显示名
     */
    String label;
    /**
     * 数据值
     */
    String value;
    /**
     * 下级数据
     */
    List<SelectedAttribute> children;

}
