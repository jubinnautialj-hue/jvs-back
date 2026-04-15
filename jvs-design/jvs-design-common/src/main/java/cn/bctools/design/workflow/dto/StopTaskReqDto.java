package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("终止流程入参")
public class StopTaskReqDto {

    @ApiModelProperty(value = "终止任务原因")
    private String reason;
    
    @ApiModelProperty(value = "任务ID列表（用于批量终止）")
    private java.util.List<String> taskIds;
}
