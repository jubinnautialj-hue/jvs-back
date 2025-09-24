package cn.bctools.design.workflow.service.impl;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.service.JvsAppVersionService;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.dto.SelfApproveLogReqDto;
import cn.bctools.design.workflow.dto.SelfApproveLogResDto;
import cn.bctools.design.workflow.entity.FlowTask;
import cn.bctools.design.workflow.entity.FlowTaskApprovalRecord;
import cn.bctools.design.workflow.mapper.FlowTaskApprovalRecordMapper;
import cn.bctools.design.workflow.service.FlowTaskApprovalRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 工作流审批记录
 */
@Slf4j
@Service
@AllArgsConstructor
public class FlowTaskApprovalRecordServiceImpl extends ServiceImpl<FlowTaskApprovalRecordMapper, FlowTaskApprovalRecord>  implements FlowTaskApprovalRecordService {

    private final JvsAppVersionService appVersionService;

    @Override
    public void saveUnique(FlowTask flowTask, UserDto userDto) {
        // 不保存测试流程的审批记录
        if (Boolean.TRUE.equals(flowTask.getTest())) {
            return;
        }
        FlowTaskApprovalRecord flowTaskApprovalRecord = new FlowTaskApprovalRecord();
        flowTaskApprovalRecord.setFlowTaskId(flowTask.getId());
        flowTaskApprovalRecord.setUserId(userDto.getId());
        flowTaskApprovalRecord.setJvsAppId(flowTask.getJvsAppId());
        // 若审批记录已存在，则修改时间
        FlowTaskApprovalRecord old = getOne(Wrappers.<FlowTaskApprovalRecord>lambdaQuery()
                .eq(FlowTaskApprovalRecord::getFlowTaskId, flowTask.getId())
                .eq(FlowTaskApprovalRecord::getUserId, userDto.getId()));
        if (ObjectNull.isNotNull(old)) {
            old.setUpdateTime(LocalDateTime.now());
            updateById(old);
        } else {
            save(flowTaskApprovalRecord);
        }
    }

    @Override
    public void selfApproveLog(Page<SelfApproveLogResDto> page, UserDto userDto, SelfApproveLogReqDto dto) {
        // 查询当前模式应用id集合
        List<String> appIds = appVersionService.getVersionTypeAppIds(ModeUtils.getMode());
        if (CollectionUtils.isEmpty(appIds)) {
            return;
        }
        QueryWrapper queryWrapper = Wrappers.query()
                .eq("ar.user_id", userDto.getId())
                .in("ar.jvs_app_id", appIds)
                .eq(StringUtils.isNotBlank(dto.getTaskId()), "t.id", dto.getTaskId())
                .like(StringUtils.isNotBlank(dto.getTaskCode()), "t.task_code", dto.getTaskCode())
                .like(ObjectNull.isNotNull(dto.getTaskStatus()), "t.task_status", dto.getTaskStatus())
                .like(StringUtils.isNotBlank(dto.getFlowName()), "t.name", dto.getFlowName())
                .like(StringUtils.isNotBlank(dto.getTitle()), "t.title", dto.getTitle())
                .like(StringUtils.isNotBlank(dto.getSendUser()), "t.create_by", dto.getSendUser());

        baseMapper.approvalRecordPage(page, queryWrapper);
    }

    @Override
    public Boolean existsTaskApprove(UserDto user, String taskId) {
        return count(Wrappers.<FlowTaskApprovalRecord>lambdaQuery()
                .eq(FlowTaskApprovalRecord::getFlowTaskId, taskId)
                .eq(FlowTaskApprovalRecord::getUserId, user.getId())) > 0;
    }

    @Override
    public Integer selfApproveCount(List<String> appIds, UserDto userDto) {
        long count = count(Wrappers.<FlowTaskApprovalRecord>lambdaQuery()
                .eq(FlowTaskApprovalRecord::getUserId, userDto.getId())
                .in(FlowTaskApprovalRecord::getJvsAppId, appIds));
        return Long.valueOf(count).intValue();
    }

    @Override
    public List<String> queryUserIdsByTaskId(String taskId) {
        List<FlowTaskApprovalRecord> approvalRecords = list(Wrappers.<FlowTaskApprovalRecord>lambdaQuery()
                .eq(FlowTaskApprovalRecord::getFlowTaskId, taskId)
                .select(FlowTaskApprovalRecord::getUserId));
        if (CollectionUtils.isEmpty(approvalRecords)) {
            return Collections.emptyList();
        }
        return approvalRecords.stream().map(FlowTaskApprovalRecord::getUserId).distinct().collect(Collectors.toList());
    }

    @Override
    public Map<String, Set<String>> queryUserIdsByTaskId(List<String> taskIds) {
        List<FlowTaskApprovalRecord> approvalRecords = list(Wrappers.<FlowTaskApprovalRecord>lambdaQuery()
                .in(FlowTaskApprovalRecord::getFlowTaskId, taskIds)
                .select(FlowTaskApprovalRecord::getUserId, FlowTaskApprovalRecord::getFlowTaskId));
        if (CollectionUtils.isEmpty(approvalRecords)) {
            return Collections.emptyMap();
        }
        return approvalRecords.stream()
                .collect(Collectors.groupingBy(FlowTaskApprovalRecord::getFlowTaskId, Collectors.mapping(FlowTaskApprovalRecord::getUserId, Collectors.toSet())));
    }
}
