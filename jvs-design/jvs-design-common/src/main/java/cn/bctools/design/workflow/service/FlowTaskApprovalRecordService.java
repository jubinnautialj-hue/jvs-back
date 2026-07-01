package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.SelfApproveLogReqDto;
import cn.bctools.design.workflow.dto.SelfApproveLogResDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskApprovalRecord;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhuxiaokang
 * 工作流审批记录
 */
public interface FlowTaskApprovalRecordService extends IService<FlowTaskApprovalRecord> {

    /**
     * 保存工作流审批记录（任务_用户 唯一）
     *
     * @param flowTask 任务
     * @param userDto  用户
     */
    void saveUnique(FlowTask flowTask, UserDto userDto);

    /**
     * 我审批的任务记录
     *
     * @param page    分页条件
     * @param userDto 用户
     * @param dto     查询条件
     */
    void selfApproveLog(Page<SelfApproveLogResDto> page, UserDto userDto, SelfApproveLogReqDto dto);

    /**
     * 用户是否存在指定任务的审批记录
     *
     * @param user   用户
     * @param taskId 任务id
     * @return TRUE-存在审批记录，FALSE-不存在审批记录
     */
    Boolean existsTaskApprove(UserDto user, String taskId);

    /**
     * 用户参与审批的任务数量
     *
     * @param appIds  应用id集合
     * @param userDto 用户
     * @return 用户参与审批的任务数量
     */
    Integer selfApproveCount(List<String> appIds, UserDto userDto);

    /**
     * 获取指定任务的历史审批人id（去重）
     *
     * @param taskId 任务id
     * @return 审批人id
     */
    List<String> queryUserIdsByTaskId(String taskId);

    /**
     * 获取指定任务的历史审批人id（去重）
     *
     * @param taskIds 任务id集合
     * @return Map<任务id, 审批人id集合>
     */
    Map<String, Set<String>> queryUserIdsByTaskId(List<String> taskIds);

}
