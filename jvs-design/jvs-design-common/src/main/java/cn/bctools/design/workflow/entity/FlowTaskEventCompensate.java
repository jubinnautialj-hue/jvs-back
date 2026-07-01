package cn.bctools.design.workflow.entity;

import cn.bctools.design.workflow.entity.enums.FlowTaskEventTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author zhuxiaokang
 * 事件补偿
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel("工作流事件补偿")
@TableName(value = "jvs_flow_task_event_compensate", autoResultMap = true)
public class FlowTaskEventCompensate {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "事件类型")
    @TableField("event_type")
    private FlowTaskEventTypeEnum eventType;

    @ApiModelProperty(value = "事件类容")
    @TableField("event_body")
    private String eventBody;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
