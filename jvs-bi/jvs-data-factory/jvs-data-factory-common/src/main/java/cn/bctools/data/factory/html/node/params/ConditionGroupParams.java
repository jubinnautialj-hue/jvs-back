package cn.bctools.data.factory.html.node.params;

import cn.bctools.data.factory.dto.DataSourceField;
import cn.bctools.data.factory.html.NodeHtml;
import cn.bctools.data.factory.html.node.dto.ConditionGroupNodeDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("条件分组")
public class ConditionGroupParams extends NodeHtml<ConditionGroupParams> {

    private List<ConditionGroupObj> conditionGroupNode;

    @Data
    @ApiModel("条件分组入参")
    public static class ConditionGroupObj {
        @ApiModelProperty(value = "字段名称", required = true)
        private String fieldName;
        @ApiModelProperty(value = "字段", required = true)
        private DataSourceField datasourceField;
        @ApiModelProperty(value = "条件数据", required = true)
        private List<ConditionGroup> conditionGroups;
        @ApiModelProperty(value = "不满足条件时的值", required = true)
        private String dissatisfyFieldValue;
    }

    @Data
    @ApiModel("条件分组-条件")
    public static class ConditionGroup {
        @ApiModelProperty(value = "字段值-显示名称", required = true)
        private String fieldValue;
        @ApiModelProperty(value = "条件", required = true)
        private List<ConditionGroupNodeDto> conditions;

    }

}
