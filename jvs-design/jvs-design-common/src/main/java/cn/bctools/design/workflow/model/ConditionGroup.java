package cn.bctools.design.workflow.model;

import cn.bctools.design.workflow.model.enums.ConditionOperatorEnum;
import lombok.Data;

import java.util.List;

/**
 * @author zhuxiaokang
 * 条件组
 */
@Data
public class ConditionGroup {
    /**
     * 条件组关系
     */
    private ConditionOperatorEnum connection;

    /**
     *
     */
    private List<String> cids;

    /**
     * 条件集合
     */
    private List<ConditionProperties> condition;
}
