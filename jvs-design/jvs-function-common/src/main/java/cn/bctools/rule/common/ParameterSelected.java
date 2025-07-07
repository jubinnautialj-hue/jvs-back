package cn.bctools.rule.common;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;

import java.util.Collections;
import java.util.List;

/**
 * 获取这个类型的可选的几个值
 * T 泛型可以支持自定义。如果是自定义，需要添加自定义注解。
 *
 * @param <T> the type parameter
 * @author guojing
 */
public interface ParameterSelected<T> extends DefaultValueParameter {

    /**
     * 获取所有的可选项
     *
     * @return 可选项集合 options
     * @throws BusinessException the business exception
     * @author: guojing
     */
    default List<ParameterOption<T>> getOptions() throws BusinessException {
        return Collections.emptyList();
    }

    /**
     * 返回这个选择项需要的关键字
     * 为了选择根据此条件进行筛选过滤
     *
     * @return 返回关联字段的key string
     */
    default String key() {
        List<ParameterOption<T>> options = getOptions();
        if (ObjectNull.isNotNull(options)) {
            return options.get(0).getValue().toString();
        } else {
            return null;
        }
    }

}
