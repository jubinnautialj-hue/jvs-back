package cn.bctools.design.workflow.entity;

import cn.bctools.design.workflow.entity.dto.ParallelBranchDto;
import cn.bctools.design.workflow.entity.handler.ParallelBranchTypeHandler;
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
 * 工作流并行任务
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("工作流并行任务")
@TableName(value = "jvs_flow_task_parallel", autoResultMap = true)
public class FlowTaskParallel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "工作流任务id")
    @TableField("flow_task_id")
    private String flowTaskId;

    @ApiModelProperty(value = "并行节点id")
    @TableField("node_id")
    private String nodeId;

    @ApiModelProperty(value = "并行分支完成状态")
    @TableField(value = "branchs", typeHandler = ParallelBranchTypeHandler.class)
    private List<ParallelBranchDto> branchs;

    @ApiModelProperty(value = "并行分支总数")
    @TableField("branch_number")
    private Integer branchNumber;

    @ApiModelProperty(value = "已完成分支数量", notes = "表示执行完的分支数量")
    @TableField("completed_number")
    private Integer completedNumber;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "应用id")
    @TableField(value = "jvs_app_id")
    private String jvsAppId;
}
