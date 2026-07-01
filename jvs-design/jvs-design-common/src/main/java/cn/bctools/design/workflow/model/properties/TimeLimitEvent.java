package cn.bctools.design.workflow.model.properties;

import cn.bctools.design.workflow.model.enums.TimeLimitEventTypeEnum;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 超过审批期限的执行事件
 */
@Data
public class TimeLimitEvent {

    /**
     * 超过审批期限的执行事件类型
     */
    private TimeLimitEventTypeEnum type;

    /**
     * 循环发送提醒：false-不循环(只提醒一次)，true-循环发送
     */
    private Boolean loop;

    /**
     * 循环发送提醒间隔(天)
     */
    private Integer loopTime;

}
