package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.CarbonCopyReqDto;
import cn.bctools.design.workflow.dto.CarbonCopyResDto;
import cn.bctools.design.workflow.entity.FlowTaskCopied;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author zhuxiaokang
 * 工作流抄送
 */
public interface FlowTaskCarbonCopyService extends IService<FlowTaskCopied> {

    /**
     * 分页查询抄送我的工作流
     *
     * @param page    分页
     * @param userDto 当前登陆人
     * @param dto     查询条件
     */
    void carbonCopys(Page<CarbonCopyResDto> page, UserDto userDto, CarbonCopyReqDto dto);

    /**
     * 抄送给用户的任务数量
     *
     * @param appIds  应用id集合
     * @param userDto 用户
     * @return 抄送给用户的任务数量
     */
    Integer carbonCopyCount(List<String> appIds, UserDto userDto);

    /**
     * 获取指定任务的所有抄送人id（去重）
     *
     * @param taskId 任务id
     * @return 抄送人id
     */
    List<String> queryUserIdsByTaskId(String taskId);


    /**
     * 获取指定任务的所有抄送人id（去重）
     *
     * @param taskIds 任务id集合
     * @return Map<任务id, 抄送人id集合>
     */
    Map<String, Set<String>> queryUserIdsByTaskId(List<String> taskIds);

    /**
     * 用户是否存在指定任务的抄送记录
     *
     * @param user   用户
     * @param taskId 任务id
     * @return TRUE-存在抄送记录，FALSE-不存在抄送记录
     */
    Boolean exists(UserDto user, String taskId);
}
