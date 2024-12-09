package cn.bctools.design.workflow.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 流转节点操作类型枚举
 */

@Getter
@AllArgsConstructor
public enum NodeOperationTypeEnum {

    /**
     * 审批处理类型
     */
    PASS("PASS", "同意"),
    REFUSE("REFUSE", "拒绝"),
    BACK("BACK", "回退"),
    SAVE("SAVE", "保存"),
    TRANSFER("TRANSFER", "转交"),
    APPEND("APPEND", "加签"),
    TERMINATED("TERMINATED", "终止"),
    REMOVE_SIGNER("REMOVE_SIGNER", "移除审批人"),
    ADD_SIGNER("ADD_SIGNER", "增加审批人"),
    ;

    @JsonValue
    private String value;
    private String desc;

}
