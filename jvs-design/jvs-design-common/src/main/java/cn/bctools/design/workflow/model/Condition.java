package cn.bctools.design.workflow.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author zhuxiaokang
 * 条件分支
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Condition extends Node{

    /**
     * TRUE-默认条件分支，FALSE-普通条件分支
     */
    private Boolean defaultCondition = Boolean.FALSE;

    /**
     * 条件组集合
     */
    private List<ConditionGroup> groups;
}
