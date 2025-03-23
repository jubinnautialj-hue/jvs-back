package cn.bctools.chart.chart.bo;


import cn.bctools.chart.chart.po.ChartDesignInParameter;
import cn.bctools.chart.enums.ChartElementTypeEnums;
import cn.bctools.data.factory.dto.OrderField;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * setting的公共参数
 *
 * @author admin
 */

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChartSettingBo {
    /**
     * 数据源 需要在执行逻辑时手动设置
     */
    private JSONObject dataSource;

    /**
     * 图表类型
     */
    private ChartElementTypeEnums type;
    /**
     * 数据钻取 配置信息
     */
    private List<DrillSetting> drillSetting;
    /**
     * 过滤条件
     */
    private List<ChartFilterJsonBo.FilterData> searchFilterJson;
    /**
     * 组内条件
     */
    private ChartFilterJsonBo dataFilterJson;

    /**
     * 排序字段
     */
    private List<OrderField> sortList;

    /**
     * 数据的设置 例如:柱状图 x y分别是什么字段
     */
    private ChartDesignInParameter logicSetting;
    /**
     * 获取数据的模式
     * 0 正常模式 1,表格模式 2，下载数据
     */
    private Integer getDataType;
    /**
     * 表格模式
     * 当前页码
     */
    private Long current;
    /**
     * 下载文件名称
     */
    private String fileName;
    /**
     * 表格模式和条数限制
     * 每页数量
     */
    private Long size;


}
