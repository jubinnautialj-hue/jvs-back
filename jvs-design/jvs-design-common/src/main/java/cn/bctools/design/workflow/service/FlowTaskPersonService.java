package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.entity.FlowTaskPerson;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流任务待办人 服务类
 */
public interface FlowTaskPersonService extends IService<FlowTaskPerson> {

    /**
     * 删除工作流任务所有待办人信息
     *
     * @param flowTaskId 流程任务id
     */
    void removeTaskAll(String flowTaskId);

    /**
     * 删除工作流指定节点的所有待办人信息
     *
     * @param flowTaskId 流程任务id
     * @param nodeId     节点id
     */
    void removeTaskNodeAll(String flowTaskId, String nodeId);

    /**
     * 用户的待办任务数量
     *
     * @param appIds  应用id集合
     * @param userDto 用户
     * @return 用户的待办任务数量
     */
    Integer pendingApprovesCount(List<String> appIds, UserDto userDto);

    /**
     * 节点待审批人id
     *
     * @param flowTaskId 任务id
     * @param nodeIds    节点id集合
     * @return 待审批人id集合
     */
    List<String> getPendingApproveUserIds(String flowTaskId, List<String> nodeIds);

    /**
     * 节点待审批人id
     *
     * @param flowTaskId 任务id
     * @param nodeId     节点id
     * @return 待审批人id集合
     */
    List<UserDto> getPendingApproveUsers(String flowTaskId, String nodeId);

    /**
     * 用户是否有指定节点的待审批任务
     *
     * @param flowTaskId 任务id
     * @param nodeId     节点id
     * @param userId     用户id
     * @return TRUE-有待审批任务，FALSE-无待审批任务
     */
    Boolean checkPendingTask(String flowTaskId, String nodeId, String userId);

    /**
     * 批量获取任务待审批人集合
     *
     * @param taskIds 任务id集合
     * @return 待审批人集合
     */
    List<FlowTaskPerson> listPendingByTaskIds(List<String> taskIds);

    /**
     * 节点审批人
     *
     * @param flowTaskId 任务id
     * @param nodeIds    节点id集合
     * @return 审批人集合
     */
    List<FlowTaskPerson> listPerson(String flowTaskId, List<String> nodeIds);


    /**
     * 任务审批人
     *
     * @param taskIds 任务id集合
     * @return 审批人集合
     */
    List<FlowTaskPerson> listPerson(List<String> taskIds);
}
