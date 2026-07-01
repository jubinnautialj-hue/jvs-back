package cn.bctools.design.project.entity;

import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
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

/**
 * @author hrl
 */
@Getter
@Setter
@ApiModel("应用模板创建或迭代应用任务进度详情")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("jvs_app_template_task_progress_detail")
public class JvsAppTemplateTaskProgressDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("模板任务id")
    private String taskId;
    @ApiModelProperty("任务编码")
    private String code;
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("异常栈")
    private String exceptionStackTrace;
    @ApiModelProperty("状态")
    private AppTemplateTaskProgressEnum progress;
    @ApiModelProperty("耗时（ms）")
    private Long duration;
    @ApiModelProperty("序号")
    private Integer serialNumber;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String tenantId;
}
