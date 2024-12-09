package cn.bctools.design.workflow.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 节点审批类型
 */
@Getter
@AllArgsConstructor
public enum FlowTaskNodeApprovalTypeEnum {

    /**
     * 非加签审批
     */
    APPROVAL(1, "审批"),
    APPEND_APPROVAL(2, "加签审批"),
    ;

    @EnumValue
    @JsonValue
    private final Integer value;
    private String desc;
}
