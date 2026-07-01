package cn.bctools.design.workflow.dto;

import cn.bctools.common.entity.dto.UserDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author jvs
 * 工作流审批人
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class FlowApprovalUserDTO extends UserDto {

    /**
     * 审批顺序
     */
    private Integer approvalSequence;
}
