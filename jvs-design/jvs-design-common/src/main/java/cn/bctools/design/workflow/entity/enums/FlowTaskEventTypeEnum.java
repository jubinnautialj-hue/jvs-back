package cn.bctools.design.workflow.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 流程事件类型
 */
@Getter
@AllArgsConstructor
public enum FlowTaskEventTypeEnum {
    /**
     * 同步流程数据到业务数据事件
     */
    ASYNC_TASK_TO_DATA("ASYNC_TASK_TO_DATA", "同步流程数据到业务数据事件"),
    ;

    @EnumValue
    @JsonValue
    private String value;
    private String desc;


}
