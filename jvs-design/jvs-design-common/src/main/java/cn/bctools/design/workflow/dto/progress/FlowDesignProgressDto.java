package cn.bctools.design.workflow.dto.progress;

import cn.bctools.design.workflow.model.Node;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("任务进度响应-工作流设计JSON(含处理过程)")
public class FlowDesignProgressDto extends Node {

    @ApiModelProperty(value = "当前节点")
    private Boolean currentNode = Boolean.FALSE;
}
