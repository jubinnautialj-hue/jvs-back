package cn.bctools.design.workflow.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

/**
 * @author zhuxiaokang 
 */
@Data
@Accessors(chain = true)
@ApiModel("获取已发布工作流审批节点设计响应")
public class ApproveNodesDto {

    @ApiModelProperty("true-可以增加节点，false-不能增加节点")
    private Boolean canDynamicAddNode = Boolean.FALSE;

    @ApiModelProperty("节点集合")
    private List<ApproveNodeDto> nodes = Collections.emptyList();
}
