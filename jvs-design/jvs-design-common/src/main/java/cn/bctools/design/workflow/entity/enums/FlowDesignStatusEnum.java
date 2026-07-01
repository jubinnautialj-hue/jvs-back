package cn.bctools.design.workflow.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 工作流设计状态
 */
@Getter
@AllArgsConstructor
public enum FlowDesignStatusEnum {

    /**
     * 工作流设计状态
     */
    UNPUBLISHED(1, "未发布"),
    PUBLISHED(2, "已发布"),
    STOP(3, "已停用"),
    ;

    @EnumValue
    @JsonValue
    private Integer value;
    private String desc;
}
