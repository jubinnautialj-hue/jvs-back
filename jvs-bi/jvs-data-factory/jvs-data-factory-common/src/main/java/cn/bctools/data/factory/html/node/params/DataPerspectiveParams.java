package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.NodeHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("数据透视节点")
public class DataPerspectiveParams extends NodeHtml<DataPerspectiveParams> {

    private DataPerspectiveObj dataPerspectiveObj;

    @Data
    @ApiModel("数据透视节点入参")
    public static class DataPerspectiveObj {
        @ApiModelProperty(value = "行分类", required = true)
        private DataSourceField rowField;
        @ApiModelProperty(value = "统计字段", required = true)
        private DataSourceField statisticsField;
        @ApiModelProperty(value = "列分类", required = true)
        private DataSourceField columnField;
    }

}
