package cn.bctools.data.factory.html.node.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 数据填充时的中间流转数据
 *
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
public class DataFillDto {
    /**
     * 当前需要获取第几个字段的值
     */
    private int fieldIndex;
}
