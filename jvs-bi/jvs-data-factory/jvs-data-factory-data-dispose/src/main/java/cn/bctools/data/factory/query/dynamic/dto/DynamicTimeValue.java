package cn.bctools.data.factory.query.dynamic.dto;

import cn.hutool.core.date.DateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 处理后的返回结果
 *
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class DynamicTimeValue {
    /**
     * 开始时间
     */
    private DateTime startTime;
    /**
     * 结束时间
     */
    private DateTime endTime;
}
