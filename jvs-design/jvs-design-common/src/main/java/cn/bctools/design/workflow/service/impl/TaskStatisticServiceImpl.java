package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.FlowTaskStatisticResDto;
import cn.bctools.design.workflow.service.*;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhuxiaokang
 * 工作流任务统计
 */
@Service
@AllArgsConstructor
public class TaskStatisticServiceImpl implements TaskStatisticService {
    private final FlowTaskPersonService flowTaskPersonService;
    private final FlowTaskCarbonCopyService flowTaskCarbonCopyService;
    private final FlowTaskService flowTaskService;
    private final FlowTaskApprovalRecordService flowTaskApprovalRecordService;
    private final JvsAppVersionService appVersionService;

    @Override
    public FlowTaskStatisticResDto statistic(UserDto userDto) {
        FlowTaskStatisticResDto resDto = new FlowTaskStatisticResDto();
        // 查询当前模式所有应用id
        List<String> appIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        if (CollectionUtils.isEmpty(appIds)) {
            return resDto;
        }
        // 我的待办任务数量
        resDto.setPendingCount(flowTaskPersonService.pendingApprovesCount(appIds, userDto));
        // 我创建的任务数量
        resDto.setSelfCreateCount(flowTaskService.selfCreateCount(appIds, userDto));
        // 抄送给我的任务数量
        resDto.setCarbonCopyCount(flowTaskCarbonCopyService.carbonCopyCount(appIds, userDto));
        // 我参与审批的任务数量
        resDto.setSelfApproveCount(flowTaskApprovalRecordService.selfApproveCount(appIds, userDto));
        return resDto;
    }
}
