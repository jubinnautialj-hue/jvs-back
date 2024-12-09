package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 节点类型分组
 */
@Getter
@AllArgsConstructor
public enum NodeTypeGroupEnum {

    /**
     * 节点类型分组
     */
    MANUAL("MANUAL", "人工"),
    AUTO("AUTO", "自动执行"),
    OTHER("OTHER", "其它"),
    ;

    private String value;
    private String desc;
}
