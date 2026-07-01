package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.model.Node;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("校验是否允许在当前节点后动态增加节点响应")
public class CheckDynamicNodeDto {

    @ApiModelProperty("true-可以增加节点，false-不能增加节点")
    private Boolean canDynamicAddNode = Boolean.FALSE;

    @ApiModelProperty("新加的节点")
    private Node node;
}
