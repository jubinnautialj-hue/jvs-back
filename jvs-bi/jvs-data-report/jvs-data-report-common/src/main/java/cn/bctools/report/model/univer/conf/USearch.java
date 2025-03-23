package cn.bctools.report.model.univer.conf;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("查询条件")
public class USearch {

    @ApiModelProperty("数据集id")
    private String executeName;

    @ApiModelProperty("表名称")
    private String tableName;

    @ApiModelProperty("字段列表")
    private List<USearchField> fields;
}
