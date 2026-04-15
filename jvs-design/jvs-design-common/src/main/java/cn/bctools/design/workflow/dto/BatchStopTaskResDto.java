package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 批量终止流程响应DTO
 */
@Data
@Accessors(chain = true)
@ApiModel("批量终止流程响应")
public class BatchStopTaskResDto {

    @ApiModelProperty(value = "成功终止的任务数量")
    private Integer successCount;

    @ApiModelProperty(value = "失败的任务数量")
    private Integer failCount;

    @ApiModelProperty(value = "失败详情：任务ID -> 失败原因")
    private Map<String, String> failDetails;

    @ApiModelProperty(value = "成功终止的任务ID列表")
    private List<String> successTaskIds;
}
