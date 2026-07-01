package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流设计DTO
 */
@Slf4j
@Data
@Accessors(chain = true)
public class FlowDesignDto {

    @ApiModelProperty("工作流id")
    private String id;

    @ApiModelProperty(value = "工作流名称")
    private String name;

    @ApiModelProperty("数据模型id")
    private String dataModelId;

    @ApiModelProperty("分类")
    private String designGroup;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("应用ID")
    private String jvsAppId;

    @ApiModelProperty("是否已发布：0-未发布，1-已发布")
    private Boolean published;

    @ApiModelProperty(value = "租户id")
    private String tenantId;

    @ApiModelProperty(value = "工作流设计节点集合")
    private List<FlowDesignNodeDto> nodes;
}
