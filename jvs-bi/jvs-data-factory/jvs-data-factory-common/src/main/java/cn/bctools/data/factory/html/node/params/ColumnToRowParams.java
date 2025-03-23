package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.NodeHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
public class ColumnToRowParams extends NodeHtml<ColumnToRowParams> {

    private ColumnToRowObj columnToRowObj;

    @Data
    @ApiModel("列转行入参")
    public static class ColumnToRowObj {
        @ApiModelProperty(value = "列转行字段", required = true)
        private List<DataSourceField> columnToRowList;
        @ApiModelProperty(value = "列转行-新的字段名称", required = true)
        private String columnToRowKeyNewName;
        @ApiModelProperty(value = "列转行-新的字段key", required = true)
        private String columnToRowKeyNewKey;
        @ApiModelProperty(value = "列转行-新的字段名称", required = true)
        private String columnToRowValueNewName;
        @ApiModelProperty(value = "列转行-新的字段key", required = true)
        private String columnToRowValueNewKey;
    }
}
