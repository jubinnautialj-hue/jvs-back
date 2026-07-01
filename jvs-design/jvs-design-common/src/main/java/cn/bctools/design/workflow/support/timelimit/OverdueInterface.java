package cn.bctools.design.workflow.support.timelimit;

import cn.bctools.design.workflow.entity.FlowTaskPerson;
import cn.bctools.design.workflow.model.enums.TimeLimitEventTypeEnum;

/**
 * @author zhuxiaokang
 * 审批期限预期执行
 */
public interface OverdueInterface {

    /**
     * 处理类型
     * @return 类型
     */
    TimeLimitEventTypeEnum getType();

    /**
     * 逾期处理
     * @param msg 消息
     * @param flowTaskPerson 工作流任务待办人信息
     */
    void process(TimeLimitMessageDto msg, FlowTaskPerson flowTaskPerson);
}
