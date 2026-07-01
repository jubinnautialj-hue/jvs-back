package cn.bctools.design.data.fields.dto.page;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <列表页设计-数据设计>
 *
 * @author auto
 **/
@Data
@Accessors(chain = true)
public class DataPageDesignHtml {
    @ApiModelProperty(value = "数据库名称")
    private String databaseName;
    @ApiModelProperty("表格配置")
    private List<DataTableFieldDesignHtml> autoTableFields;
    @ApiModelProperty(value = "查询条件JSON传参格式", notes = "数据交互格式")
    private String queryJson;
    @ApiModelProperty(value = "列表数据渲染JSON格式", notes = "数据交互格式")
    private String dataPageJson;
}
