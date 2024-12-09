package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.dto.startflow.SelfSelectApprover;
import cn.bctools.design.workflow.entity.dto.ApproveOpinionDto;
import cn.bctools.design.workflow.entity.dto.TransferDto;
import cn.bctools.design.workflow.model.Node;
import com.alibaba.fastjson2.JSONObject;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流处理操作信息入参")
public class FlowReqDto {

    @ApiModelProperty(value = "工作流任务id", required = true)
    @NotBlank(message = "工作流任务id不能为空")
    private String id;

    @ApiModelProperty(value = "工作流任务节点id", required = true)
    @NotBlank(message = "工作流任务节点id不能为空")
    private String nodeId;

    @ApiModelProperty(value = "流转节点操作类型", notes = "人工审核必传")
    private NodeOperationTypeEnum nodeOperationType;

    @ApiModelProperty(value = "审批意见")
    private ApproveOpinionDto opinion;

    @ApiModelProperty(value = "回退节点id", notes = "回退操作必传")
    private String backNodeId;

    @ApiModelProperty(value = "内容JSON")
    private JSONObject data;

    @ApiModelProperty(value = "转交", notes = "转交操作必传")
    private TransferDto transfer;

    @ApiModelProperty(value = "加签", notes = "加签操作必传")
    private AppendApprovalOperationDto appendApproval;

    @ApiModelProperty(value = "自选审批人", notes = "允许流转过程中，指定后续节点的审批人")
    private List<SelfSelectApprover> approvers;

    @ApiModelProperty(value = "动态增加的节点")
    private Node node;

}
