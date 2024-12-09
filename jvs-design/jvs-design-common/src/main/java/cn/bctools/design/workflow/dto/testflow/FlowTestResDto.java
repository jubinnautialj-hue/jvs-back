package cn.bctools.design.workflow.dto.testflow;

import cn.bctools.design.workflow.entity.dto.FlowManualNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedList;

/**
 * @author zhuxiaokang
 * 测试工作流处理操作信息响应
 */
@Data
@Accessors(chain = true)
@ApiModel("测试工作流处理操作信息响应")
public class FlowTestResDto {

    @ApiModelProperty(value = "流转的人工节点")
    private LinkedList<FlowManualNode> flowManualNodes;
}
