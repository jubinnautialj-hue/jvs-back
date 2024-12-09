package cn.bctools.design.workflow.model.properties;

import cn.bctools.design.workflow.model.enums.NodePropertiesEndConditionEnum;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 领导
 */
@Data
public class Leader {

    /**
     * 发起人的几级主管（直接主管为第1级主管）
     */
    private Integer leaderLevel;

    /**
     * 审批终点：TOP-全部，LEAVE-指定领导级别
     */
    private NodePropertiesEndConditionEnum endCondition;
}
