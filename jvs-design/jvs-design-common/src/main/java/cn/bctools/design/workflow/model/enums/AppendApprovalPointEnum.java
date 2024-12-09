package cn.bctools.design.workflow.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 加签位置
 */
@Getter
@AllArgsConstructor
public enum AppendApprovalPointEnum {

    /**
     * 加签位置
     */
    BEFORE("BEFORE", "前加签"),
    AFTER("AFTER", "后加签"),
    ;

    @EnumValue
    @JsonValue
    private final String value;
    private String desc;
}
