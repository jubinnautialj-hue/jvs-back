package cn.bctools.design.workflow.support.timelimit.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.entity.dto.ApproveOpinionDto;
import cn.bctools.design.workflow.enums.NodeOperationTypeEnum;
import cn.bctools.design.workflow.model.enums.TimeLimitEventTypeEnum;
import cn.bctools.design.workflow.service.TaskService;
import cn.bctools.design.workflow.support.timelimit.OverdueInterface;
import cn.bctools.design.workflow.support.timelimit.TimeLimitMessageDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 自动通过
 */
@Component
@AllArgsConstructor
public class AutoPassOverdueImpl implements OverdueInterface {

    private final TaskService taskService;

    @Override
    public TimeLimitEventTypeEnum getType() {
        return TimeLimitEventTypeEnum.PASS;
    }

    @Override
    public void process(TimeLimitMessageDto msg, FlowTaskPerson flowTaskPerson) {
        // 封装处理用户
        UserDto userDto = new UserDto();
        userDto.setId(flowTaskPerson.getUserId());
        userDto.setRealName(flowTaskPerson.getUserName());
        // 自动审核-通过
        FlowReqDto flowDto = new FlowReqDto()
                .setId(flowTaskPerson.getFlowTaskId())
                .setNodeId(flowTaskPerson.getNodeId())
                .setNodeOperationType(NodeOperationTypeEnum.PASS)
                .setOpinion(new ApproveOpinionDto().setContent("超时自动审批"));
        taskService.execute(flowDto, userDto);
    }
}
