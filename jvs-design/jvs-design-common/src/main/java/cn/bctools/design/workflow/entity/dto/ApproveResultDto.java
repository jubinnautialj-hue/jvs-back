package cn.bctools.design.workflow.entity.dto;

import cn.bctools.design.workflow.dto.AppendApprovalOperationDto;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 审批结果
 */
@Data
@Accessors(chain = true)
@ApiModel("审批结果")
public class ApproveResultDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "操作类型")
    private NodeOperationTypeEnum nodeOperationTypeEnum;

    @ApiModelProperty(value = "审批意见")
    private ApproveOpinionDto opinion;

    @ApiModelProperty(value = "用户处理时间")
    private String time;

    @ApiModelProperty(value = "移交记录")
    private ProxyDto transfer;

    @ApiModelProperty(value = "加签记录")
    private AppendApprovalOperationDto appendApproval;

    /**
     * 加签后，一个节点会有多批审批人处理。用于区分已处理和未处理的审批人
     */
    @ApiModelProperty(value = "TRUE-处理结束，FALSE-处理中")
    private boolean over = Boolean.FALSE;

    @ApiModelProperty(value = "回退目标节点id", notes = "只有回退操作需要记录回退到了那个节点")
    private String backNodeId;
}
