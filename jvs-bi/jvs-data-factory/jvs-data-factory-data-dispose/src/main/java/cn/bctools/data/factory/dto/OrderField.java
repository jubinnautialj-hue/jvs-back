package cn.bctools.data.factory.dto;

import cn.bctools.data.factory.enums.SortTypeEnums;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 排序数据
 */
@Data
@Accessors(chain = true)
public class OrderField {
    /**
     * 排序key
     */
    private String fieldKey;
    /**
     * 排序类型
     */
    private SortTypeEnums sortType;
}
