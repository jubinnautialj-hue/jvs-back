package cn.bctools.design.workflow.support.function.impl;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.design.workflow.constant.SystemConstant;
import cn.bctools.design.workflow.enums.BackTaskResubmitEnum;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.FlowTaskParallelService;
import cn.bctools.design.workflow.support.FlowResult;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.enums.FlowNextTypeEnum;
import cn.bctools.design.workflow.support.function.AbstractFunctionHandler;
import cn.bctools.design.workflow.support.function.dto.BackResubmitDto;
import cn.bctools.design.workflow.support.function.dto.ParallelDto;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowUtil;
import cn.bctools.redis.utils.RedisUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author zhuxiaokang
 * 校验并行节点是否可继续流转
 */
@Slf4j
@Component
@AllArgsConstructor
public class ParallelFunction extends AbstractFunctionHandler<Boolean, ParallelDto> {

    /**
     * 需要 校验并行节点是否可继续流转的类型
     */
    private static final List<FlowNextTypeEnum> CHECK_TYPE = Arrays.asList(FlowNextTypeEnum.END, FlowNextTypeEnum.NEXT);
    /**
     * 并行锁key
     */
    private static final String PARALLEL_LOCK = "parallellock";
    private static final Integer LOCK_EXPIRE_TIME = 10000;

    private final RedisUtils redisUtils;
    private final FlowTaskParallelService flowTaskParallelService;


    /**
     * 校验并行节点是否可继续流转
     *
     * @param node 下一个节点
     * @param parallel 运行时数据
     * @return TRUE-可继续流转，FALSE-不可继续流转
     */
    @Override
    public Boolean invoke(Node node, ParallelDto parallel) {
        BackResubmitDto backResubmit = Optional.ofNullable(parallel.getBackResubmitDto()).orElseGet(BackResubmitDto::new);
        if (Boolean.TRUE.equals(backResubmit.getWhetherResubmit()) && BackTaskResubmitEnum.DIRECT_CURRENT_NODE.equals(backResubmit.getBackTaskResubmit())) {
            return Boolean.TRUE;
        }

        FlowResult flowResult = parallel.getFlowResult();
        // 部分操作需要校验
        if (Boolean.FALSE.equals(CHECK_TYPE.contains(flowResult.getFlowNextTypeEnum()))) {
            return Boolean.TRUE;
        }
        // 终止操作，可继续流转
        if (FlowNextTypeEnum.END.equals(flowResult.getFlowNextTypeEnum())) {
            return Boolean.TRUE;
        }
        RuntimeData runtimeData = FlowContextUtil.context().getRuntimeData();
        // 当前节点
        Node currentNode = runtimeData.getCurrentNode();
        // 当前节点非并行节点（无并行标识的节点）可继续流转
        if (StringUtils.isBlank(currentNode.getParallelFlag())) {
            return Boolean.TRUE;
        }
        // 下一个节点的并行标识与当前节点的id或并行标识相同，可继续流转
        if (currentNode.getId().equals(node.getParallelFlag()) || currentNode.getParallelFlag().equals(node.getParallelFlag())) {
            return Boolean.TRUE;
        }
        // 判断并行分支是否全部完成
        Boolean result;
        String lockKey = getLockKey(runtimeData.getFlowTask().getId(), currentNode.getParallelFlag());
        try {
            while (Boolean.FALSE.equals(redisUtils.tryLock(lockKey, LOCK_EXPIRE_TIME))) {
                // 未获取到锁，则等一会儿再获取
                Thread.sleep(10);
            }
            Node branchNode = FlowUtil.getParallelBranchNode(currentNode);
            result = flowTaskParallelService.checkCompleted(runtimeData.getFlowTask().getId(), currentNode.getParallelFlag(), branchNode.getId());
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        } finally {
            redisUtils.unLock(lockKey);
        }
        return result;
    }


    /**
     * 并行锁key
     *
     * @param taskId 工作流任务id
     * @param nodeId 工作流节点id
     * @return 并行锁key
     */
    private String getLockKey(String taskId, String nodeId) {
        return SystemConstant.SYSTEM_NAME + ":" + PARALLEL_LOCK + ":" + taskId + ":" + nodeId;
    }
}
