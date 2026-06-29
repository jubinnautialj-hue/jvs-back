package cn.bctools.function.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class ExpressionExtendDto {
    @ApiModelProperty("逻辑引擎节点id")
    String nodeId;
    @ApiModelProperty("逻辑引擎画布id")
    String canvasId;
    @ApiModelProperty("逻辑引擎线的id")
    String graphId;
}
