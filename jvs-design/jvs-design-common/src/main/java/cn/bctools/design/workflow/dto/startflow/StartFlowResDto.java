package cn.bctools.design.workflow.dto.startflow;

import cn.bctools.design.workflow.entity.FlowTask;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 发起工作流任务响应
 */
@Data
@Accessors(chain = true)
@ApiModel("发起工作流任务响应")
public class StartFlowResDto {
    @ApiModelProperty(value = "工作流id")
    private String flowTaskId;

    @ApiModelProperty(value = "内容")
    private JSONObject data;

    @ApiModelProperty(value = "工作流任务")
    private FlowTask flowTask;
}
