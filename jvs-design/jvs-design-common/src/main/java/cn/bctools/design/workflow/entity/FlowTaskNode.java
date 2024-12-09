package cn.bctools.design.workflow.entity;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.workflow.entity.dto.AppendApprovalDto;
import cn.bctools.design.workflow.entity.dto.CourseDto;
import cn.bctools.design.workflow.entity.enums.FlowTaskNodeApprovalTypeEnum;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.entity.handler.TaskNodeApprovalPersonHandler;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流流转节点
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("工作流流转节点")
@TableName(value = "jvs_flow_task_node", autoResultMap = true)
public class FlowTaskNode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "工作流任务id")
    @TableField("flow_task_id")
    private String flowTaskId;

    @ApiModelProperty(value = "工作流节点id")
    @TableField("node_id")
    private String nodeId;

    @ApiModelProperty(value = "处理过程")
    @TableField(value = "course", typeHandler = Fastjson2TypeHandler.class)
    private CourseDto course;

    @ApiModelProperty(value = "1-挂起，0-正常")
    @TableField("hang")
    private Boolean hang;

    @ApiModelProperty(value = "状态：1-待处理，2-已处理")
    @TableField("process_status")
    private ProcessStatusEnum processStatus;

    @ApiModelProperty(value = "审批类型：1-审批，2-加签审批")
    @TableField("approval_type")
    private FlowTaskNodeApprovalTypeEnum approvalType;

    @ApiModelProperty(value = "当前节点审批人")
    @TableField(value = "approval_persons", typeHandler = TaskNodeApprovalPersonHandler.class)
    private List<UserDto> approvalPersons;

    @ApiModelProperty(value = "加签配置")
    @TableField(value = "append_approval", typeHandler = Fastjson2TypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private AppendApprovalDto appendApproval;

    @ApiModelProperty(value = "临时存储新的下级节点")
    @TableField(value = "temp_new_node", updateStrategy = FieldStrategy.IGNORED)
    private String tempNewNode;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    @ApiModelProperty(value = "应用id")
    @TableField(value = "jvs_app_id")
    private String jvsAppId;
}
