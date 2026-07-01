package cn.bctools.design.workflow.support.runtime.impl;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskPath;
import cn.bctools.design.workflow.entity.dto.FlowPathNodeDto;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.FlowTaskPathService;
import cn.bctools.design.workflow.support.RuntimeData;
import cn.bctools.design.workflow.support.context.FlowContext;
import cn.bctools.design.workflow.support.runtime.RuntimeTaskPathService;
import cn.bctools.design.workflow.utils.FlowContextUtil;
import cn.bctools.design.workflow.utils.FlowPathUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Component
@AllArgsConstructor
public class RuntimeTaskPathServiceImpl implements RuntimeTaskPathService {

    private FlowTaskPathService flowTaskPathService;

    @Override
    public void refreshTaskPath(FlowTask flowTask) {
        FlowContext flowContext = FlowContextUtil.context().getContext();
        List<List<Node>> paths = FlowPathUtil.getNodePaths(flowTask.getDesignBody());
        List<FlowTaskPath> flowTaskPaths = paths.stream()
                .map(path -> new FlowTaskPath().setFlowTaskId(flowTask.getId()).setPath(BeanCopyUtil.copys(path, FlowPathNodeDto.class)).setJvsAppId(flowTask.getJvsAppId()))
                .collect(Collectors.toList());
        // 不是启动工作流，则直接入库
        if (Boolean.FALSE.equals(flowContext.getStart())) {
            flowTaskPathService.removeTaskPath(flowTask.getId());
            flowTaskPathService.save(flowTask);
        }
        // 刷新上下文
        RuntimeData runtimeData = flowContext.getRuntimeData().setTaskPaths(flowTaskPaths);
        FlowContextUtil.refreshContext(runtimeData);
    }
}
