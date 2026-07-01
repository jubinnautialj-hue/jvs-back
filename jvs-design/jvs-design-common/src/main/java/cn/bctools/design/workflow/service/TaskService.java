package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.CanBackNodeDto;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.dto.FlowTaskManageSignerRemoveReqDto;
import cn.bctools.design.workflow.dto.FlowTaskManageTransferReqDto;
import cn.bctools.design.workflow.dto.startflow.SelfSelectApprover;
import cn.bctools.design.workflow.dto.startflow.StartFlowReqDto;
import cn.bctools.design.workflow.dto.startflow.StartFlowResDto;
import cn.bctools.design.workflow.dto.startflow.StartFlowVariables;
import cn.bctools.design.workflow.entity.FlowTask;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @author zhuxiaokang
 * 工作流任务流转服务
 */
public interface TaskService {

    /**
     * 启动工作流
     *
     * @param userDto   用户
     * @param variables 启动工作流变量
     * @return 工作流任务id
     */
    StartFlowResDto start(UserDto userDto, StartFlowVariables variables);

    /**
     * 执行工作流
     *
     * @param flowDto 审批入参
     * @param userDto 审批人
     * @return 工作流执行对象
     */
    FlowTask execute(FlowReqDto flowDto, UserDto userDto);

    /**
     * 批量执行工作流
     *
     * @param flowReqDto 工作流启动对象
     * @param userDto    当前启动人员信息
     * @return 返回是否执行成功, 异步
     */
    Future<Boolean> batchExecute(FlowReqDto flowReqDto, UserDto userDto);

    /**
     * 重新发起工作流任务
     *
     * @param dto     入参
     * @param userDto 用户
     * @return 重启后的任务对象
     */
    StartFlowResDto restartTask(StartFlowReqDto dto, UserDto userDto);

    /**
     * 获取可回退的节点
     *
     * @param taskId 任务id
     * @param nodeId 发起回退的节点
     * @return 可回退节点集合
     */
    List<CanBackNodeDto> getCanBackNode(String taskId, String nodeId);


    /**
     * 是否有未结束的任务
     *
     * @param businessId 业务id
     * @return true-有未结束的任务，false-任务都已结束
     */
    Boolean havePendingTask(String businessId);

    /**
     * 查询未结束的任务
     *
     * @param businessId 业务id
     * @return 任务
     */
    FlowTask getPendingTask(String businessId);

    /**
     * 增员审批
     * <p>
     *     向已有待审批节点中指派新的审批人。
     *     只对本次审批生效，若有回退等操作，重新到达该节点，不会自动将新指派的用户作为审批人
     *
     * @param userDto 用户
     * @param taskId 指派
     * @param approvers 指派审批人
     */
    void assign(UserDto userDto, String taskId, List<SelfSelectApprover> approvers);

    /**
     * 任务转交
     * <p>
     *     将任务当前所有待审批节点中指定的未审批用户任务转交给其它人
     * @param userDto 用户
     * @param taskId 任务id
     * @param transferList 转交
     */
    void transfer(UserDto userDto, String taskId, List<FlowTaskManageTransferReqDto> transferList);

    /**
     * 减员审批
     *
     * @param userDto 用户
     * @param taskId 任务id
     * @param removeSigners 减少的审批人员
     */
    void removeSigner(UserDto userDto, String taskId, List<FlowTaskManageSignerRemoveReqDto> removeSigners);
}
