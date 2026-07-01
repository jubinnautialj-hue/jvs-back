package cn.bctools.design.workflow.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 工作流任务状态
 */
@Getter
@AllArgsConstructor
public enum FlowTaskStatusEnum implements IEnum {

    /**
     * 工作流任务状态
     */
    PENDING(1, "待审批"),
    PASSED(2, "已通过"),
    REJECTED(3, "已拒绝"),
    /**
     * 人工操作强行终止流程
     */
    TERMINATED(4, "已终止"),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;
    private String desc;

}
