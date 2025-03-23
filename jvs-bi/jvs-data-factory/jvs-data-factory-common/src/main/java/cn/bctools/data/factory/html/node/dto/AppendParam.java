package cn.bctools.data.factory.html.node.dto;

import cn.bctools.data.factory.dto.DataSourceField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class AppendParam extends DataSourceField {

    @ApiModelProperty("包含的节点对应字段")
    private Map<String, DataSourceField> stageFieldMap;

}
