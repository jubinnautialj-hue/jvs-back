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
@ApiModel("分页查询工作流入参")
public class PageFlowDesignReqDto {

    @ApiModelProperty(value = "工作流名称")
    private String name;

    @ApiModelProperty(value = "工作流分类")
    private String designGroup;
}
