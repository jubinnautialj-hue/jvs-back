package cn.bctools.design.workflow.entity;

import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 工作流任务待办人
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("工作流任务待办人")
@TableName(value = "jvs_flow_task_person", autoResultMap = true)
public class FlowTaskPerson implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "工作流任务id")
    @TableField("flow_task_id")
    private String flowTaskId;

    @ApiModelProperty(value = "节点id")
    @TableField("node_id")
    private String nodeId;

    @ApiModelProperty(value = "人员id")
    @TableField("user_id")
    private String userId;

    @ApiModelProperty(value = "人员姓名")
    @TableField("user_name")
    private String userName;

    @ApiModelProperty(value = "1-挂起，0-正常")
    @TableField("hang")
    private Boolean hang;

    @ApiModelProperty(value = "状态：1-待处理，2-已处理")
    @TableField("process_status")
    private ProcessStatusEnum processStatus;

    @ApiModelProperty(value = "审批顺序")
    @TableField("number")
    private Integer number;

    @ApiModelProperty(value = "是否是测试任务 0否  1是")
    @TableField("test")
    private Boolean test;

    @ApiModelProperty(value = "应用id")
    @TableField(value = "jvs_app_id")
    private String jvsAppId;

    @ApiModelProperty(value = "人员账号，待办需要使用")
    @TableField(exist = false)
    private String accountName;
}
