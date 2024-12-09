package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskParallel;
import cn.bctools.design.workflow.entity.dto.ParallelBranchDto;
import cn.bctools.design.workflow.mapper.FlowTaskParallelMapper;
import cn.bctools.design.workflow.service.FlowTaskParallelService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流并行任务 服务实现类
 */
@Slf4j
@Service
public class FlowTaskParallelServiceImpl extends ServiceImpl<FlowTaskParallelMapper, FlowTaskParallel> implements FlowTaskParallelService {

    @Override
    public void init(FlowTask flowTask, String parallelNodeId, List<ParallelBranchDto> branchs) {
        FlowTaskParallel parallel = new FlowTaskParallel()
                .setFlowTaskId(flowTask.getId())
                .setJvsAppId(flowTask.getJvsAppId())
                .setNodeId(parallelNodeId)
                .setBranchs(branchs)
                .setBranchNumber(branchs.size())
                .setCompletedNumber(0);
        saveOrUpdate(parallel, Wrappers.<FlowTaskParallel>lambdaUpdate()
                .eq(FlowTaskParallel::getFlowTaskId, flowTask.getId())
                .eq(FlowTaskParallel::getNodeId, parallelNodeId));
    }

    @Override
    public Boolean checkCompleted(String taskId, String parallelNodeId, String branchNodeId) {
        FlowTaskParallel flowTaskParallel = getOne(Wrappers.<FlowTaskParallel>lambdaQuery()
                .eq(FlowTaskParallel::getFlowTaskId, taskId)
                .eq(FlowTaskParallel::getNodeId, parallelNodeId));
        if (ObjectNull.isNull(flowTaskParallel)) {
            return Boolean.TRUE;
        }
        flowTaskParallel.setCompletedNumber(flowTaskParallel.getCompletedNumber() + 1);
        flowTaskParallel.getBranchs().forEach(branch -> {
            // 设置当前分支已完成
            if (branch.getBranchId().equals(branchNodeId)) {
                branch.setComplete(Boolean.TRUE);
            }
        });
        updateById(flowTaskParallel);
        return flowTaskParallel.getCompletedNumber().equals(flowTaskParallel.getBranchNumber());
    }

    @Override
    public void removeTaskAll(String flowTaskId) {
        remove(Wrappers.<FlowTaskParallel>lambdaQuery().eq(FlowTaskParallel::getFlowTaskId, flowTaskId));
    }
}
