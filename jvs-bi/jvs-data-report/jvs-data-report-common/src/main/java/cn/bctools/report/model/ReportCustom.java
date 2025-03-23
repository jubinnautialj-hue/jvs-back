package cn.bctools.report.model;

import cn.bctools.report.model.dataSource.DataSource;
import cn.bctools.report.model.univer.UCell;
import cn.bctools.report.model.univer.conf.USearch;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author wl
 */
@Data
@Accessors(chain = true)
@ApiModel("报表配置")
public class ReportCustom {

    @ApiModelProperty("数据源")
    private List<DataSource> dataSources;

    @ApiModelProperty("单元格设置")
    private List<UCell> cellSettings;

    @ApiModelProperty(value = "多数据源关联条件",example = "{子表名称:[{leftColumn:main.key,rightColumn:children.key}]}",notes = "Map<String,List<JoinSetting>>")
    private Map<String,List<JoinSetting>> joinSettings;

    @ApiModelProperty(value = "分页设置",notes = "分页设置")
    private Map<String,Object> pages;

    @ApiModelProperty(value = "查询设置",notes = "查询条件")
    private List<USearch> queryConf;
}
