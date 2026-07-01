package cn.bctools.design.workflow.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 处理状态
 */
@Getter
@AllArgsConstructor
public enum ProcessStatusEnum {

    /**
     * 工作流任务状态
     */
    PREPARE(0, "准备中"),
    PENDING(1, "待处理"),
    PROCESSED(2, "已处理"),
    ;

    @EnumValue
    @JsonValue
    private Integer value;
    private String desc;
}
