package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskPath;
import cn.bctools.design.workflow.entity.dto.FlowPathNodeDto;
import cn.bctools.design.workflow.mapper.FlowTaskPathMapper;
import cn.bctools.design.workflow.model.Node;
import cn.bctools.design.workflow.service.FlowTaskPathService;
import cn.bctools.design.workflow.utils.FlowPathUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流任务可执行路径
 */
@Service
public class FlowTaskPathServiceImpl extends ServiceImpl<FlowTaskPathMapper, FlowTaskPath> implements FlowTaskPathService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(FlowTask flowTask) {
        List<List<Node>> paths = FlowPathUtil.getNodePaths(flowTask.getDesignBody());
        List<FlowTaskPath> flowTaskPaths = paths.stream()
                .map(path -> new FlowTaskPath().setFlowTaskId(flowTask.getId()).setPath(BeanCopyUtil.copys(path, FlowPathNodeDto.class)).setJvsAppId(flowTask.getJvsAppId()))
                .collect(Collectors.toList());
        saveBatch(flowTaskPaths);
    }

    @Override
    public void removeTaskPath(String taskId) {
        remove(Wrappers.<FlowTaskPath>lambdaQuery().eq(FlowTaskPath::getFlowTaskId, taskId));
    }

    @Override
    public List<List<Node>> getNodePaths(FlowTask flowTask, String nodeId) {
        // 从数据库获取路径，若数据库中没有则从设计中获取路径（兼容已启动且未结束的工作流任务）
        List<List<Node>> paths = null;
        List<FlowTaskPath> flowTaskPaths = list(Wrappers.<FlowTaskPath>lambdaQuery().eq(FlowTaskPath::getFlowTaskId, flowTask.getId()));
        if (CollectionUtils.isNotEmpty(flowTaskPaths)) {
            paths = flowTaskPaths.stream().map(flowTaskPath -> BeanCopyUtil.copys(flowTaskPath.getPath(), Node.class)).collect(Collectors.toList());
        } else {
            paths = FlowPathUtil.getNodePaths(flowTask.getDesignBody());
        }
        // 获取包含当前节点的可执行路径
        return paths.stream().filter(path -> path.stream().anyMatch(node -> nodeId.equals(node.getId()))).collect(Collectors.toList());
    }
}
