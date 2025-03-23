package cn.bctools.design.workflow.model.properties;

import lombok.Data;

import java.util.Set;

/**
 * @author jvs
 * 主管来源——流程节点配置
 */
@Data
public class LeaderFlowNodeSource {

    /**
     * 流程节点id集合
     */
    private Set<String> nodeIds;
}
