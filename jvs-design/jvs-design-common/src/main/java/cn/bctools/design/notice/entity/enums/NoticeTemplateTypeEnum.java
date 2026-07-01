package cn.bctools.design.notice.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 消息模板类型
 */
@Getter
@AllArgsConstructor
public enum NoticeTemplateTypeEnum {
    /**
     * 模型消息模板
     */
    MODEL("MODEL", "模型消息模板"),
    ;

    @EnumValue
    @JsonValue
    private String value;
    private String desc;
}
