package cn.bctools.design.workflow.dto;

import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhuxiaokang
 * 工作流设计节点DTO
 */
@Slf4j
@Data
@Accessors(chain = true)
public class FlowDesignNodeDto {

    @ApiModelProperty("节点id")
    private String id;

    @ApiModelProperty("节点名称")
    private String name;

    @ApiModelProperty("节点类型")
    private NodeTypeEnum type;

    @ApiModelProperty(value = "审批人员类型", notes = "审批节点有数据")
    private NodePropertiesTypeEnum approverType;
}
