package cn.bctools.report.model.dataSource;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("数据源")
public class DataSource {

    @ApiModelProperty("数据源id")
    private String dataSourceId;

    @ApiModelProperty("表名称")
    private String executeName;

    @ApiModelProperty("是否有子类")
    private String hasChildren;

    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("数据源名称")
    private String name;

    @ApiModelProperty("数据源描述")
    private String tableNameDesc;

    @ApiModelProperty("包含的字段")
    private List<Field> fields;

    @ApiModelProperty(value = "是否为主表",notes = "主表只有一个")
    private boolean isMain;

}
