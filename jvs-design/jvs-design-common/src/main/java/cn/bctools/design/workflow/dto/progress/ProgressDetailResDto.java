package cn.bctools.design.workflow.dto.progress;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.entity.enums.FlowTaskStatusEnum;
import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhuxiaokang 
 */
@Data
@Accessors(chain = true)
@ApiModel("任务进度响应")
public class ProgressDetailResDto {

    @ApiModelProperty(value = "任务创建时间")
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务编号")
    private String taskCode;

    @ApiModelProperty(value = "任务标题")
    private String taskTitle;

    @ApiModelProperty(value = "任务状态")
    private FlowTaskStatusEnum taskStatus;

    @ApiModelProperty(value = "进度节点信息")
    private List<ProgressNodeDetailDto> nodes;

    @ApiModelProperty(value = "进度")
    private List<FlowTaskProgressDto> flowTaskProgress;

    @ApiModelProperty(value = "工作流设计JSON(含处理过程)")
    private FlowDesignProgressDto flowDesign;

    @ApiModelProperty(value = "参与流程的所有用户")
    private List<UserDto> users;

    @ApiModelProperty(value = "应用id")
    private String jvsAppId;

    @ApiModelProperty(value = "工作流设计高级配置")
    private FlowExtendDto extend;
}
