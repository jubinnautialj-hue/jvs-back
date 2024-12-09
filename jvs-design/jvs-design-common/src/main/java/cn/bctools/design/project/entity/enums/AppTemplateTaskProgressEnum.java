package cn.bctools.design.project.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author hrl
 */
@Getter
@AllArgsConstructor
public enum AppTemplateTaskProgressEnum {
    /**
     * 待处理
     */
    WAIT("WAIT"),
    /**
     * 处理中
     */
    PROCESSING("PROCESSING"),
    /**
     * 成功
     */
    SUCCESS("SUCCESS"),
    /**
     * 失败
     */
    FAILURE("FAILURE"),
    ;


    @EnumValue
    @JsonValue
    private String value;
}
