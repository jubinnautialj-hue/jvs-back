package cn.bctools.design.workflow.dto.testflow;

import cn.bctools.design.workflow.entity.dto.FlowManualNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedList;

/**
 * @author zhuxiaokang 
 */
@Data
@Accessors(chain = true)
@ApiModel("发起测试工作流响应")
public class StartFlowTestResDto {

    @ApiModelProperty(value = "工作流任务id")
    private String taskId;

    @ApiModelProperty(value = "数据id")
    private String dataId;

    @ApiModelProperty(value = "流转的人工节点")
    private LinkedList<FlowManualNode> flowManualNodes;

}
