package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.entity.FlowDesign;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("工作流设计详情")
public class FlowDesignDetailDto extends FlowDesign {

    @ApiModelProperty(value = "工作流列表页挂载的菜单id")
    private String menuId;

    @ApiModelProperty(value = "工作流设计")
    private String designBody;
}
