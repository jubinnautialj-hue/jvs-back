package cn.bctools.design.workflow.model.properties;

import cn.bctools.design.workflow.model.enums.LeaderSourceEnum;
import cn.bctools.design.workflow.model.enums.NodePropertiesEndConditionEnum;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 审批主管配置
 */
@Data
public class Leader {

    /**
     * 主管来源。 为兼容旧数据，默认主管来源为发起人
     */
    private LeaderSourceEnum leaderSource = LeaderSourceEnum.SEND_USER;

    /**
     * 主管来源——成员字段配置
     * <p>
     *     主管来源为成员字段时，根据成员字段中的用户查找主管
     */
    private Target userFieldConfig;

    /**
     * 主管来源——流程节点配置
     * <p>
     *     主管来源为流程节点时，根据流程节点实际参与审批的审批人查找主管
     */
    private LeaderFlowNodeSource flowNodeConfig;

    /**
     * 发起人的几级主管（直接主管为第1级主管）
     */
    private Integer leaderLevel;

    /**
     * 审批终点：TOP-全部，LEAVE-指定领导级别
     */
    private NodePropertiesEndConditionEnum endCondition;
}
