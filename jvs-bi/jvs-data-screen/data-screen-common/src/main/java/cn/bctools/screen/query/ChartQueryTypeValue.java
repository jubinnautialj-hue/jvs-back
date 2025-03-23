package cn.bctools.screen.query;

/**
 * 用户条件值获取
 * 主要是用于动态数据值例如:当前用户id 当前用户部门id 当前用户 角色id 字典值
 */
public interface ChartQueryTypeValue {

    /**
     * 默认返回传入的值
     * 注意如果需要其它值 传入json 对应的处理类 执行处理
     */
    String execute(String value);
}
