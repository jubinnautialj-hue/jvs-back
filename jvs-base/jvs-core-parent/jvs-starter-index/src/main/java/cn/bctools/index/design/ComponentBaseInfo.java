package cn.bctools.index.design;

import cn.bctools.index.design.enums.ComponentType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 组件属性描述声明
 *
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class ComponentBaseInfo {

    /**
     * 组件唯一标识
     * 每个组件的唯一标识不能相同 相同会影响缓存数据
     * 不设置唯一标识 缓存选项不生效
     */
    String key;
    /**
     * spring 的bean ，可能一个组件会存在多个类型
     */
    String bean;
    /**
     * 组件类型
     */
    ComponentType type;
    /**
     * 图标
     */
    String icon = "icon-shizhong";
    /**
     * 组件名称
     */
    String name;

    /**
     * 组件描述
     */
    String description;

    /**
     * 组件需要显示的表单属性
     */
    List<FormAttribute> formAttributes;
    /**
     * 实体类定义规则,与 formAttributes 二选一定义即可
     */
    String formQueryParamsBaseClass;

    /**
     * 是否开启缓存 默认false
     */
    Boolean enableCache = Boolean.FALSE;

    /**
     * 缓存间隔时间 秒 默认 60
     */
    Long intervalTime = 60L;

}
