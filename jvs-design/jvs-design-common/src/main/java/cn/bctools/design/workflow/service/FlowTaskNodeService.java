package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskNode;
import cn.bctools.design.workflow.entity.dto.ProxyDto;
import cn.bctools.design.workflow.entity.enums.ProcessStatusEnum;
import cn.bctools.design.workflow.model.Node;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * The interface Flow task node service.
 *
 * @author zhuxiaokang  工作流流转节点 服务类
 */
public interface FlowTaskNodeService extends IService<FlowTaskNode> {

    /**
     * 保存当前节点处理结果
     *
     * @param processStatusEnum 节点处理状态
     * @return 返回任务节点信息 flow task node
     */
    FlowTaskNode saveResult(ProcessStatusEnum processStatusEnum);

    /**
     * 为节点处理过程赋值（不保存）
     *
     * @param flowTaskNode 流程任务节点
     */
    void assignCourse(FlowTaskNode flowTaskNode);

    /**
     * 得到任务正在处理的所有节点
     *
     * @param taskId 任务id
     * @return 待办节点集合 current nodes by task id
     */
    List<FlowTaskNode> getCurrentNodesByTaskId(String taskId);

    /**
     * 得到任务正在处理的节点
     *
     * @param taskId 任务id
     * @param nodeId 节点id
     * @return 状态为处理中的任务节点 current pending node
     */
    FlowTaskNode getCurrentPendingNode(String taskId, String nodeId);

    /**
     * 批量得到任务正在处理的节点
     *
     * @param taskIds 任务id集合
     * @return 待办节点集合 current node by task ids
     */
    List<FlowTaskNode> getCurrentNodeByTaskIds(List<String> taskIds);

    /**
     * 得到任务正在处理的节点
     *
     * @param taskId  任务id
     * @param nodeIds 指定节点id
     * @return 待办节点集合 current nodes
     */
    List<FlowTaskNode> getCurrentNodes(String taskId, List<String> nodeIds);

    /**
     * 得到任务节点
     *
     * @param taskId  任务id
     * @param nodeIds 指定节点id
     * @return 待办节点集合 task nodes
     */
    List<FlowTaskNode> getTaskNodes(String taskId, List<String> nodeIds);

    /**
     * 判断指定节点是否是目标工作流任务当前处理的节点
     *
     * @param taskId 工作流任务id
     * @param nodeId 节点id
     * @return TRUE -是，FALSE-否
     */
    boolean whetherCurrentNode(String taskId, String nodeId);

    /**
     * 保存下一节点
     *
     * @param node     下一节点信息
     * @param flowTask 任务信息
     * @return the flow task node
     */
    FlowTaskNode saveNextNode(Node node, FlowTask flowTask);

    /**
     * 保存下一节点
     *
     * @param node     下一节点信息
     * @param flowTask 任务信息
     * @param userList 当前节点审批用户
     * @return the flow task node
     */
    FlowTaskNode saveNextNode(Node node, FlowTask flowTask, List<UserDto> userList);

    /**
     * 保存转交记录
     *
     * @param proxyList 转交集合
     * @param taskId    任务id
     * @param nodeId    节点id
     */
    void saveTransfer(List<ProxyDto> proxyList, String taskId, String nodeId);

    /**
     * 设置所有处理结果的状态为结束
     *
     * @param flowTaskNode 待办节点
     */
    void updateCourseApproveOver(FlowTaskNode flowTaskNode);


    /**
     * 删除工作流任务所有节点
     *
     * @param flowTaskId 流程任务id
     */
    void removeTaskAll(String flowTaskId);
}
