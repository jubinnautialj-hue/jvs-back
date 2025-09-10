package cn.bctools.design.taskNotice.entity;

import cn.bctools.database.entity.po.BasalPo;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wayne
 * 待办提醒发送日志
 */
@Data
@Accessors(chain = true)
@ApiModel("待办提醒对照表")
@TableName(value = "jvs_flow_task_notice", autoResultMap = true)
public class FlowTaskNotice extends BasalPo {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "业务实例ID")
    @TableField("instance_id")
    private String instanceId;

    @ApiModelProperty(value = "工作流节点id")
    @TableField("node_id")
    private String nodeId;

    @ApiModelProperty(value = "单据ID")
    @TableField("task_id")
    private String taskId;

    @ApiModelProperty(value = "业务审批对照ID")
    @TableField("biz_task_id")
    private String bizTaskId;

    @ApiModelProperty(value = "待办应用ID")
    @TableField(value = "app_id")
    private String appId;

    @ApiModelProperty(value = "应用id")
    @TableField(value = "jvs_app_id")
    private String jvsAppId;

    @ApiModelProperty(value = "处理状态：0待处理，1已关闭，2撤回")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty(value = "是否删除 0未删除  1已删除")
    @TableField("del_flag")
    @TableLogic
    private Boolean delFlag;
}
