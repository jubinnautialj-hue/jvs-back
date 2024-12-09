package cn.bctools.design.workflow.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 权限组
 */

@Getter
@AllArgsConstructor
public enum PurviewGroupEnum {
    /**
     * 发起工作流权限
     */
    send_flow("send_flow", "发起工作流权限"),
    ;

    @EnumValue
    @JsonValue
    private final String value;
    private String desc;
}
