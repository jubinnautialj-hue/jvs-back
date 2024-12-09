package cn.bctools.design.workflow.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 节点类型
 */
@Getter
@AllArgsConstructor
public enum NodeTypeEnum {

    /**
     * 节点类型，节点类型分组，节点类型描述，节点默认名，节点默认id
     */
    ROOT("ROOT", NodeTypeGroupEnum.OTHER, "起始节点", "发起人" ,"10000"),
    EMPTY("EMPTY", NodeTypeGroupEnum.OTHER, "空节点", null, null),
    CONDITION("CONDITION", NodeTypeGroupEnum.OTHER,"条件节点", null, null),
    TJ("TJ", NodeTypeGroupEnum.OTHER, "条件分支", null, null),
    PARALLEL("PARALLEL", NodeTypeGroupEnum.OTHER,"并行节点", null, null),
    PB("PB", NodeTypeGroupEnum.OTHER, "并行分支", null, null),

    SP("SP", NodeTypeGroupEnum.MANUAL, "审批节点", null, null),
    CS("CS",  NodeTypeGroupEnum.AUTO, "抄送节点", null, null),
    AUTOMATION("AUTOMATION", NodeTypeGroupEnum.AUTO,"集成&自动化", null, null),
    ;

    private String value;
    private NodeTypeGroupEnum group;
    private String desc;
    private String defaultNodeName;
    private String defaultNodeId;

    public static NodeTypeEnum getByValue(String value) {
        for (NodeTypeEnum currentEnum : NodeTypeEnum.values()) {
            if (currentEnum.value.equals(value)) {
                return currentEnum;
            }
        }
        return null;
    }

}
