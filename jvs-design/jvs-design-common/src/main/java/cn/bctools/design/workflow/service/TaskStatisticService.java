package cn.bctools.design.workflow.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.workflow.dto.FlowTaskStatisticResDto;

/**
 * @author zhuxiaokang
 * 工作流任务统计
 */
public interface TaskStatisticService {

    /**
     * 统计
     *
     * @param userDto 用户
     * @return 统计结果
     */
    FlowTaskStatisticResDto statistic(UserDto userDto);
}
