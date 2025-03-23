package cn.bctools.data.factory.entity;

import cn.bctools.data.factory.entity.enums.OperateMethodEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskStatusEnum;
import cn.bctools.data.factory.entity.enums.QueueTaskTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 数据etl-队列记录表
 * </p>
 *
 * @author wl
 * @since 2022-08-18
 */
@Data
@ApiModel("数据etl-队列记录表")
@Accessors(chain = true)
@TableName(value = "jvs_data_factory_queue")
public class JvsDataFactoryQueue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("id")
    private String id;

    @ApiModelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("负责人")
    private String principalName;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("优先级")
    private Integer priority;

    @ApiModelProperty("执行图")
    private String executionGraph;

    @ApiModelProperty("结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @ApiModelProperty("设计id")
    @TableField("data_factory_id")
    private String dataFactoryId;

    @ApiModelProperty("批次id")
    @TableField("batch_id")
    private String batchId;

    @ApiModelProperty("任务状态")
    private QueueTaskStatusEnum taskStatus;

    @ApiModelProperty("更新方式 auto 自动 manual 手动")
    @TableField("operate_method")
    private OperateMethodEnum operateMethod;

    @ApiModelProperty("任务类型")
    @TableField("queue_task_type")
    private QueueTaskTypeEnum queueTaskType;

    @ApiModelProperty("如果是前置或者后置 就表示本身id")
    @TableField("task_itself_id")
    private String taskItselfId;

    @ApiModelProperty("租户id")
    private String tenantId;

    @ApiModelProperty("执行进度")
    private String executeSchedule;

    @ApiModelProperty("失败原因")
    private String errorMsg;

    @ApiModelProperty("智仓名称")
    @TableField(exist = false)
    private String taskName;

    @ApiModelProperty("任务本身名称用于前置任务后置任务是否")
    @TableField(exist = false)
    private String taskItselfName;


}
