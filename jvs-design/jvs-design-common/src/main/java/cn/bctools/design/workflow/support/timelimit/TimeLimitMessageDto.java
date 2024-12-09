package cn.bctools.design.workflow.support.timelimit;

import cn.bctools.design.workflow.model.properties.TimeLimit;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 审批期限超时校验消息
 */
@Data
public class TimeLimitMessageDto {
    /**
     * 工作流任务待办人主键id
     */
    private String flowTaskPersonId;

    /**
     * 工作流任务名
     */
    private String flowTaskName;

    /**
     * 发起人名称
     */
    private String sendUserName;

    /**
     * 审批期限配置
     */
    private TimeLimit timeLimit;
}
