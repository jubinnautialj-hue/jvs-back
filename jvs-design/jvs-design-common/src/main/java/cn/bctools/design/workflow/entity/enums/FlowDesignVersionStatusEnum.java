package cn.bctools.design.workflow.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 工作流设计版本
 */
@Getter
@AllArgsConstructor
public enum FlowDesignVersionStatusEnum {

    /**
     * 使用： 一个工作流设计同时只能有一个版本为"使用"状态
     */
    USE("USE"),
    /**
     * 历史
     */
    HISTORY("HISTORY"),
    /**
     * 设计中： 一个工作流设计同时只能有一个版本为"设计中"状态
     */
    DESIGNING("DESIGNING"),
    ;

    @EnumValue
    @JsonValue
    private String value;
}