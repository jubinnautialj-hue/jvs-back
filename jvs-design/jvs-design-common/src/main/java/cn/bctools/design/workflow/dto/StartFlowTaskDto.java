package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 启动工作流入参
 */
@Slf4j
@Data
@Accessors(chain = true)
public class StartFlowTaskDto {

    @ApiModelProperty(value = "工作流id", required = true)
    @NotBlank(message = "工作流id不能为空")
    private String id;

    @ApiModelProperty(value = "内容")
    private Map<String, Object> data;

    @ApiModelProperty(value = "数据id")
    private String dataId;

    @ApiModelProperty(value = "数据模型id")
    private String modelId;
}
