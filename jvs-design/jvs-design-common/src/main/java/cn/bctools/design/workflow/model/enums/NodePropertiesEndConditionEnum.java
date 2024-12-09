package cn.bctools.design.workflow.model.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 审批终点
 */
@Getter
@AllArgsConstructor
public enum NodePropertiesEndConditionEnum {

    /**
     * 审批终点
     */
    TOP("TOP", "全部"),
    LEAVE("LEAVE", "指定领导级别"),
    ;

    private String value;
    private String desc;
}
