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
public class RowToColumnParams extends NodeHtml<RowToColumnParams> {

    private RowToColumnObj rowToColumnObj;

    @Data
    @ApiModel("行转列入参")
    public static class RowToColumnObj {
        @ApiModelProperty(value = "分组字段", required = true)
        private List<DataSourceField> groupList;
        @ApiModelProperty(value = "保留字段 注意保留字段与分组还有统计字段不能一样", required = true)
        private List<DataSourceField> retainList;
        @ApiModelProperty(value = "列字段", required = true)
        private DataSourceField columnField;
        @ApiModelProperty(value = "汇总", required = true)
        private Polymerization polymerization;
    }
}
