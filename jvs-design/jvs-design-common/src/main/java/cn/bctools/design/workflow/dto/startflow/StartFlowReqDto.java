package cn.bctools.design.workflow.dto.startflow;

import cn.bctools.design.workflow.model.Node;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author zhuxiaokang
 * 发起工作流任务入参
 */
@Data
@ApiModel("发起工作流任务入参")
public class StartFlowReqDto {

    @ApiModelProperty(value = "工作流id/工作流任务id", required = true)
    @NotBlank(message = "工作流id/工作流任务id不能为空")
    private String id;

    @ApiModelProperty(value = "内容JSON")
    private JSONObject data;

    @ApiModelProperty(value = "自选审批人")
    private List<SelfSelectApprover> approvers;

    @ApiModelProperty(value = "动态增加的节点")
    private Node node;
}
