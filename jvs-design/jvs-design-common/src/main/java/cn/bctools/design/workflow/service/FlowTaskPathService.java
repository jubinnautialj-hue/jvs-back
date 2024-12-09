package cn.bctools.design.workflow.service;

import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskPath;
import cn.bctools.design.workflow.model.Node;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流任务可执行路径
 */
public interface FlowTaskPathService extends IService<FlowTaskPath> {

    /**
     * 保存工作流任务可执行路径
     *
     * @param flowTask 任务
     */
    void save(FlowTask flowTask);

    /**
     * 删除任务所有路径
     *
     * @param taskId 任务id
     */
    void removeTaskPath(String taskId);

    /**
     * 获取指定节点的所有可执行路径
     *
     * @param flowTask 任务
     * @param nodeId   节点id
     * @return 可执行路径路径集合
     */
    List<List<Node>> getNodePaths(FlowTask flowTask, String nodeId);
}
