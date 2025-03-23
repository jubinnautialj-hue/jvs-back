package cn.bctools.report.model.univer;

import cn.bctools.report.model.ReportCustom;
import cn.bctools.report.model.univer.conf.*;
import cn.bctools.report.model.univer.style.UStyle;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@ApiModel("univer 工作表")
public class USheet implements Serializable {

    private static final long serialVersionUID = 2028490620611727397L;

    @ApiModelProperty("工作表的唯一标识符")
    private String id;

    @ApiModelProperty("工作表的名称")
    private String name;

    @ApiModelProperty("工作表标签的颜色")
    private String tabColor;

    @ApiModelProperty(value = "univer报表类型",notes = "0 GRID, 1 KANBAN, 2 GANTT")
    private Integer type = 0;

    @ApiModelProperty("工作表是否隐藏。默认值：BooleanNumber.FALSE")
    private Boolean hidden = Boolean.FALSE;

    @ApiModelProperty("冻结窗格设置")
    private UFreeze freeze;

    @ApiModelProperty("总行数")
    private Integer rowCount;

    @ApiModelProperty("总列数")
    private Integer columnCount;

    @ApiModelProperty("列的默认宽度")
    private Integer defaultColumnWidth;

    @ApiModelProperty("行的默认高度")
    private Integer defaultRowHeight;

    @ApiModelProperty("合并单元格范围的数组")
    private List<URange> mergeData;

    /**
     * {
     *   cellData: {
     *     // 第一行
     *     0: {
     *       // 第一列
     *       0: { v: 'A1' },
     *       // 第二列
     *       1: { v: 'B1' },
     *     },
     *     // 第二行
     *     1: {
     *       // 第一列
     *       0: { v: 'A2' },
     *       // 第二列
     *       1: { v: 'B2' },
     *     },
     *   }
     * }
     */
    @ApiModelProperty(value = "单元格内容的矩阵",notes = "Map<Integer, Map<Integer,UCell>>")
    private Map<Integer, Map<Integer,UCell>> cellData;

    @ApiModelProperty(value = "行数据对象的数组",notes = "Map<Integer,URowData>")
    private Map<Integer,URowData> rowData;

    @ApiModelProperty(value = "列数据对象的数组",notes = "Map<Integer,UColumnData>")
    private Map<Integer,UColumnData> columnData;

    @ApiModelProperty("行标题配置")
    private URowHeader rowHeader;

    @ApiModelProperty("列标题配置")
    private UColumnHeader columnHeader;

    @ApiModelProperty("是否显示网格线")
    private Boolean showGridlines;

    @ApiModelProperty("工作表是否为从右到左模式")
    private Boolean rightToLeft;

    @ApiModelProperty("默认工作表样式")
    private UStyle defaultStyle;

    @ApiModelProperty("自定义设置")
    private ReportCustom custom;

}
