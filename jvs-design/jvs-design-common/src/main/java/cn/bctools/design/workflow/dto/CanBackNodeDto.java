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
@ApiModel("可回退的节点")
public class CanBackNodeDto {

    @ApiModelProperty(value = "节点id")
    private String id;

    @ApiModelProperty(value = "节点名称")
    private String name;
}
