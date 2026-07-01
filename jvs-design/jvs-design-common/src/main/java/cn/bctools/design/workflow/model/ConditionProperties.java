package cn.bctools.design.workflow.model;

import cn.bctools.design.workflow.model.enums.ConditionPropertiesTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * @author zhuxiaokang
 * 条件配置
 */
@Data
public class ConditionProperties {
    /**
     * 条件id
     */
    private String id;

    /**
     * 条件名
     */
    private String name;

    /**
     * 条件类型
     */
    private ConditionPropertiesTypeEnum type;

    /**
     * 条件值集合
     * {@link cn.bctools.design.workflow.model.condition}
     */
    private List<Object> values;
}
