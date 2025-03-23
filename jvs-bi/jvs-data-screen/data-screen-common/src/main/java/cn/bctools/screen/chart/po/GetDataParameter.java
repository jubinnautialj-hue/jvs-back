package cn.bctools.screen.chart.po;

import cn.bctools.data.factory.dto.OrderField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 获取数据时的统一入参
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GetDataParameter {
    /**
     * 设计参数
     */
    private ChartDesignInParameter logicSetting;
    /**
     * 数据过滤条件
     */
    private String where;
    /**
     * 查询的数据量
     */
    private Long showNumber;
    /**
     * 条件的值
     */
    private List<Object> parameter;
    /**
     * 数据集id
     */
    private String dataFactoryId;
    /**
     * 需要查询的数据表名称
     */
    private String tableName;
    /**
     * 下载的文件名称
     */
    private String fileName;
    /**
     * 需要查询的数据表名称
     */
    private List<OrderField> sortFields;
}
