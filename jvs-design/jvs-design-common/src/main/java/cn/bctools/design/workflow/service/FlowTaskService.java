package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.FlowReqDto;
import cn.bctools.design.workflow.dto.PendingApprovesReqDto;
import cn.bctools.design.workflow.dto.PendingApprovesResDto;
import cn.bctools.design.workflow.dto.progress.ProgressDetailResDto;
import cn.bctools.design.workflow.dto.progress.ProgressPrintResDto;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskProxy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流任务 服务类
 */
public interface FlowTaskService extends IService<FlowTask> {

    /**
     * 构造保存工作流任务数据
     *
     * @param flowDesignModelId 工作流设计原本的模型id
     * @param flowDesign        设计信息
     * @param design            要保存的工作流设计JSON
     * @param dataId            数据id
     * @param sendFormId        指定发起人节点表单id
     * @return 工作流任务信息
     */
    FlowTask buildSaveFlowTask(String flowDesignModelId, FlowDesign flowDesign, String design, String dataId, String sendFormId);

    /**
     * 保存工作流任务
     *
     * @param flowDesignModelId 工作流设计原本的模型id
     * @param flowDesign        工作流设计信息
     * @param design            工作流设计JSON
     * @param dataId            数据id
     * @return 工作流任务
     */
    FlowTask save(String flowDesignModelId, FlowDesign flowDesign, String design, String dataId);

    /**
     * 保存工作流任务
     *
     * @param userDto    用户信息
     * @param flowDesign 工作流设计信息
     * @param design     工作流设计JSON
     * @param dataId     数据id
     * @return 工作流任务
     */
    FlowTask saveTest(UserDto userDto, FlowDesign flowDesign, String design, String dataId);

    /**
     * 任务进度
     *
     * @param id     任务id
     * @param nodeId 节点id
     * @return 进度
     */
    ProgressDetailResDto getProgressDetail(String id, String nodeId);

    /**
     * 获取打印任务进度数据
     *
     * @param id 任务id
     * @return 进度
     */
    List<ProgressPrintResDto> getProgressPrint(String id);

    /**
     * 用户创建的任务数量
     *
     * @param appIds  应用id集合
     * @param userDto 用户
     * @return 用户创建的任务数量
     */
    Integer selfCreateCount(List<String> appIds, UserDto userDto);

    /**
     * 催办
     *
     * @param userDto 用户
     * @param taskId  流程任务id
     */
    void urge(UserDto userDto, String taskId);

    /**
     * 分页查询待我审批的工作流任务
     *
     * @param page    分页
     * @param userDto 当前登陆人
     * @param dto     查询条件
     */
    void pendingApprovePage(Page<PendingApprovesResDto> page, UserDto userDto, PendingApprovesReqDto dto);

    /**
     * 离职代理
     *
     * @param flowTaskProxy 代理配置
     */
    void departTransfer(FlowTaskProxy flowTaskProxy);

    /**
     * 修改流转中的工作流任务流程设计
     *
     * @param flowTask 工作流任务
     * @param flowDto  流转入参
     */
    void updateDesignApprover(FlowTask flowTask, FlowReqDto flowDto);

    /**
     * 工作流设计运行中的流程数量
     *
     * @param designId 工作流设计id
     * @return 工作流设计运行中的流程数量
     */
    int countPendingByDesignId(String designId);

    /**
     * 关联数据模型的流程数量
     *
     * @param modeId 数据模型id
     * @return 流程数量
     */
    int countByModeId(String modeId);

    /**
     * 查询有未结束任务的工作流设计id
     *
     * @param flowDesignIds 设计id集合
     * @return 未结束任务的设计id集合
     */
    Collection<String> pendingFlowDesignIds(Collection<String> flowDesignIds);

    /**
     * 检查是否可显示重启按钮
     *
     * @param userId     用户Id
     * @param flowTaskId 任务id
     * @return TRUE-可显示，FALSE-不可显示
     */
    boolean checkCanRestart(String userId, String flowTaskId);

    /**
     * 检查是否可显示取消按钮
     *
     * @param userId     用户Id
     * @param flowTaskId 任务id
     * @return TRUE-可显示，FALSE-不可显示
     */
    boolean checkCancel(String userId, String flowTaskId);

    /**
     * 清除工作流执行过程数据
     *
     * @param taskId 工作流任务id
     */
    void cleanTaskExecutiveProcess(String taskId);

    /**
     * 是否有未结束的任务
     *
     * @param dataId 数据id
     * @return true-有未结束的任务，false-任务都已结束
     */
    Boolean havePendingTask(String dataId);

    /**
     * 查询未结束的任务
     *
     * @param dataId 业务id
     * @return 任务
     */
    FlowTask getPendingTask(String dataId);

    /**
     * 根据数据id获取任务
     *
     * @param dataId 数据id集合
     * @return 任务集合
     */
    List<FlowTask> listByDataId(String dataId);

    /**
     * 根据数据id获取任务
     *
     * @param dataIds 数据id集合
     * @return 任务集合
     */
    List<FlowTask> listByDataIds(List<String> dataIds);


    /**
     * 根据数据id删除任务
     *
     * @param dataIds 数据id集合
     */
    void removeTaskAllByDataId(List<String> dataIds);

    /**
     * 根据数据模型id删除任务
     *
     * @param dataModelId 数据模型id
     */
    void removeTaskAllByDataModelId(String dataModelId);

    /**
     * 根据工作流任务id获取任务
     *
     * @param id 任务id
     * @return 工作流任务
     */
    FlowTask getTaskById(String id);

    /**
     * 批量填充工作流任务使用的设计
     * 得到工作流任务正在使用的工作流设计 {@link cn.bctools.design.workflow.entity.FlowTask.designBody}
     * 该设计可能根据工作流版本id获取，也可能直接获取 {@link cn.bctools.design.workflow.entity.FlowTask.flowDesign}
     *
     * @param flowTasks 工作流任务集合
     */
    void fillBatchTaskDesignBody(List<? extends FlowTask> flowTasks);

    /**
     * 填充工作流任务使用的设计
     * 得到工作流任务正在使用的工作流设计 {@link cn.bctools.design.workflow.entity.FlowTask.designBody}
     * 该设计可能根据工作流版本id获取，也可能直接获取 {@link cn.bctools.design.workflow.entity.FlowTask.flowDesign}
     *
     * @param flowTask 工作流任务
     */
    void fillTaskDesignBody(FlowTask flowTask);
}
