package cn.bctools.data.factory.query.dynamic;

import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeDto;
import cn.bctools.data.factory.query.dynamic.dto.DynamicTimeValue;

/**
 * 时间动态的值获取 抽象类
 *
 * @author xiaohui
 */
public interface DynamicTimeInterFace {

    /**
     * 根据不同的类型获取不同的值
     *
     * @param dynamicTimeDto 类型
     * @return 结果 {@link DynamicTimeValue}
     */
    DynamicTimeValue getValue(DynamicTimeDto dynamicTimeDto);

}
