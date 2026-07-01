package cn.bctools.rule.common;


/**
 * 默认值处理器 ,参数的默认值方法
 *
 * @author guojing
 */
public interface DefaultValueParameter {

    /**
     * 获取这个参数的默认值
     *
     * @return
     */
    default Object getDefaultValueParameter() {
        return null;
    }

}
