package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.FlowExtendDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.FlowDesignDynamicService;
import cn.bctools.design.workflow.service.FlowTaskNodeService;
import cn.bctools.design.workflow.service.FlowTaskService;
import cn.bctools.design.workflow.support.runtime.RuntimeTaskService;
import cn.bctools.design.workflow.utils.FlowDynamicUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.redis.utils.RedisUtils;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 动态工作流服务
 */
@Service
@AllArgsConstructor
public class FlowDesignDynamicServiceImpl implements FlowDesignDynamicService {
    /**
     * 动态改流程设计锁key
     */
    private static final String DYNAMIC_NODE_LOCK = "dynamicNodeLock";
    private static final Integer LOCK_EXPIRE_TIME = 10000;

    private final FlowTaskService flowTaskService;
    private final RuntimeTaskService taskRuntimeService;
    private final FlowTaskNodeService flowTaskNodeService;
    private final RedisUtils redisUtils;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNode(boolean updateDesign, FlowTask task, FlowExtendDto flowExtend, Node currentNode, Node newNode) {
        if (ObjectNull.isNull(newNode)) {
            return;
        }
        if (Boolean.FALSE.equals(FlowDynamicUtil.checkDynamicNode(task.getDesignBody(), flowExtend, currentNode))) {
            return;
        }
        // 增加节点
        String lockKey = getLockKey(task.getId());
        String newDesign = null;
        try {
            while (Boolean.FALSE.equals(redisUtils.tryLock(lockKey, LOCK_EXPIRE_TIME))) {
                // 未获取到锁，则等一会儿再获取
                Thread.sleep(10);
            }
            // 当前节点的后续节点有审批节点，则不可新增节点（这里再查询一次，是为了防止并行流程不同分支都在加节点导致最终设计不一致）
            FlowTask flowTask = Optional.ofNullable(flowTaskService.getById(task.getId())).orElseGet(() -> task);
            String designBody = flowTask.getDesignBody();
            List<Node> nextNodeAll = FlowUtil.getOrderNodes(designBody, currentNode.getId(), Collections.singletonList(NodeTypeEnum.SP));
            if (CollectionUtils.isNotEmpty(nextNodeAll)) {
                return;
            }
            // 设置新节点的并行节点标识（否则并进行分支下增加的节点无法处理）
            newNode.setParallelFlag(currentNode.getParallelFlag());
            // 修改设计
            if (Boolean.TRUE.equals(updateDesign)) {
                newDesign = FlowDynamicUtil.addNextNode(designBody, currentNode, newNode); 
                task.setFlowDesign(newDesign);
                task.setDesignBody(newDesign);
                taskRuntimeService.refreshDesign(task);
                FlowUtil.parseNodeJsonAndCache(newDesign);
                currentNode.setNode(FlowUtil.findNode(currentNode.getId()).getNode());
            } else {
                // 临时保存新加的节点
                FlowTaskNode flowTaskNode = new FlowTaskNode();
                flowTaskNode.setTempNewNode(JSON.toJSONString(newNode));
                flowTaskNodeService.update(flowTaskNode, Wrappers.<FlowTaskNode>lambdaUpdate()
                        .eq(FlowTaskNode::getFlowTaskId, task.getId())
                        .eq(FlowTaskNode::getNodeId, currentNode.getId()));
            }

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        } finally {
            redisUtils.unLock(lockKey);
        }
    }

    /**
     * 锁key
     *
     * @param taskId 工作流任务id
     * @return 并行锁key
     */
    private String getLockKey(String taskId) {
        return SystemConstant.SYSTEM_NAME + ":" + DYNAMIC_NODE_LOCK + ":" + taskId;
    }

}
