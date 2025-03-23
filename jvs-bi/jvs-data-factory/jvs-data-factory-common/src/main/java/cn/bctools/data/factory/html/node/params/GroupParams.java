package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.NodeHtml;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("汇总节点")
public class GroupParams extends NodeHtml<GroupParams> {

    private GroupObj groupObj;

    @Data
    @ApiModel("汇总节点入参")
    public static class GroupObj {
        @ApiModelProperty(value = "分组字段,传数据库字段", required = true)
        private List<DataSourceField> groupList;
        @ApiModelProperty(value = "汇总字段", required = true)
        private List<Polymerization> sumList;
    }
}
