package cn.bctools.rule.common;


import java.util.List;

/**
 * 关联字段的选择值
 *
 * @author guojing
 */
public interface LinkFieldSelected<T> {

    /**
     * 关联的ID值,方法名,属性值key传递的id值
     *
     * @param field 查询关联的字段
     * @param id    传递的属性key，根据key获取 下级字段
     * @return 返回关联对象
     */
    Object link(String id, T field);

    /**
     * 获取所有的字段
     *
     * @return 返回所有的字段
     */
    List<String> fields();
}
