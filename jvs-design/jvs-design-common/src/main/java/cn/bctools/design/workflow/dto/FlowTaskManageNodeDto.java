package cn.bctools.design.workflow.dto;

import cn.bctools.common.entity.dto.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
@ApiModel("工作流任务管理——任务节点信息")
public class FlowTaskManageNodeDto {
    @ApiModelProperty("节点id")
    private String nodeId;

    @ApiModelProperty("节点名")
    private String nodeName;

    @ApiModelProperty("待审批人")
    private List<UserDto> users;
}
