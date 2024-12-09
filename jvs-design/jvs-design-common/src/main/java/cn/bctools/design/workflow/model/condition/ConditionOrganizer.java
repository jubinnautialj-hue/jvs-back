package cn.bctools.design.workflow.model.condition;

import cn.bctools.design.workflow.model.enums.TargetObjectTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhuxiaokang
 * 条件类型为“发起人”的值对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConditionOrganizer extends ConditionValueBase {
    /**
     * id
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private TargetObjectTypeEnum type;

    private Boolean sex;
    private Boolean selected;
}
