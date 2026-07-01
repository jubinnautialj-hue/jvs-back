package cn.bctools.design.workflow.dto.startflow;

import cn.bctools.auth.api.dto.PersonnelDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zhuxiaokang
 * 发起工作流任务入参-自选审批人集合
 */
@Data
@ApiModel("发起工作流任务入参-自选审批人集合")
@Accessors(chain = true)
public class SelfSelectApprover {

    @ApiModelProperty(value = "节点id")
    private String nodeId;

    @ApiModelProperty(value = "自选审批人")
    private List<PersonnelDto> approvers;
}
