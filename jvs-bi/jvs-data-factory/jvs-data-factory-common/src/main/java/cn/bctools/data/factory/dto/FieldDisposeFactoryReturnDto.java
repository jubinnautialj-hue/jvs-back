package cn.bctools.data.factory.dto;


import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 内置数据类型转换返回值
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
public class FieldDisposeFactoryReturnDto {
    /**
     * 标题
     */
    List<DataSourceField> title;

    /**
     * 节点里面的数据
     */
    List<Map<String, Object>> data;
}
