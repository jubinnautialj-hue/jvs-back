package cn.bctools.design.project.service.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.dto.AppTemplateTaskProgressResDto;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgressDetail;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressDetailEnum;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import cn.bctools.design.project.mapper.JvsAppTemplateTaskProgressMapper;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressDetailService;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hrl
 */
@Service
@AllArgsConstructor
public class JvsAppTemplateTaskProgressServiceImpl extends ServiceImpl<JvsAppTemplateTaskProgressMapper, JvsAppTemplateTaskProgress> implements JvsAppTemplateTaskProgressService {

    private final JvsAppTemplateTaskProgressDetailService appTemplateTaskProgressDetailService;


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress) {
        changeProgress(taskId, taskLogEnum, progress, null);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress, Long duration) {
        changeProgress(taskId, taskLogEnum, progress, duration, null);
    }


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress, Long duration, String exceptionStackTrace) {
        appTemplateTaskProgressDetailService.update(Wrappers.<JvsAppTemplateTaskProgressDetail>lambdaUpdate()
                .set(JvsAppTemplateTaskProgressDetail::getProgress, progress)
                .set(ObjectNull.isNotNull(duration), JvsAppTemplateTaskProgressDetail::getDuration, duration)
                .set(ObjectNull.isNotNull(exceptionStackTrace), JvsAppTemplateTaskProgressDetail::getExceptionStackTrace, exceptionStackTrace)
                .eq(JvsAppTemplateTaskProgressDetail::getTaskId, taskId)
                .eq(JvsAppTemplateTaskProgressDetail::getCode, taskLogEnum.name()));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void addProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress, Long duration, String content) {
        String contentStr = content == null ? taskLogEnum.getDefaultContent() : content;
        JvsAppTemplateTaskProgressDetail templateTaskLog = new JvsAppTemplateTaskProgressDetail()
                .setTaskId(taskId)
                .setCode(taskLogEnum.name())
                .setContent(contentStr)
                .setProgress(progress)
                .setSerialNumber(taskLogEnum.getSerialNumber())
                .setDuration(duration);
        appTemplateTaskProgressDetailService.save(templateTaskLog);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void end(String taskId, AppTemplateTaskProgressEnum progress) {
        // 修改任务进度
        update(Wrappers.<JvsAppTemplateTaskProgress>lambdaUpdate()
                .set(JvsAppTemplateTaskProgress::getProgress, progress)
                .set(JvsAppTemplateTaskProgress::getUpdateTime, LocalDateTime.now())
                .eq(JvsAppTemplateTaskProgress::getId, taskId));
    }

    @Override
    public void calculateProgress(List<AppTemplateTaskProgressResDto> templateTaskProgressesDto) {
        // 得到状态为进行中的任务id，查询进度详情，计算进度
        List<String> processingTaskIds = templateTaskProgressesDto.stream()
                .filter(progress -> AppTemplateTaskProgressEnum.PROCESSING.equals(progress.getProgress()))
                .map(AppTemplateTaskProgressResDto::getId)
                .collect(Collectors.toList());
        List<JvsAppTemplateTaskProgressDetail> templateTaskProgressDetails = new ArrayList<>();
        if (ObjectNull.isNotNull(processingTaskIds)) {
            templateTaskProgressDetails = appTemplateTaskProgressDetailService.list(Wrappers.<JvsAppTemplateTaskProgressDetail>lambdaQuery()
                    .in(JvsAppTemplateTaskProgressDetail::getTaskId, processingTaskIds));
        }

        // 计算百分比
        for (AppTemplateTaskProgressResDto taskProgress : templateTaskProgressesDto) {
            // 状态为失败，默认进度百分比为0
            if (AppTemplateTaskProgressEnum.FAILURE.equals(taskProgress.getProgress())) {
                taskProgress.setRatio(0L);
                continue;
            }
            // 状态为成功，默认进度百分比为100
            if (AppTemplateTaskProgressEnum.SUCCESS.equals(taskProgress.getProgress())) {
                taskProgress.setRatio(100L);
                continue;
            }

            // 状态为进行中，计算进度百分比
            // 得到参与进度计算完成状态步骤的数量
            List<String> steps = Arrays.stream(AppTemplateTaskProgressDetailEnum.values())
                    .map(Enum::name)
                    .collect(Collectors.toList());
            double stepSuccessNum = templateTaskProgressDetails.stream()
                    .filter(detail -> detail.getTaskId().equals(taskProgress.getId()))
                    .filter(detail -> steps.contains(detail.getCode()) && AppTemplateTaskProgressEnum.SUCCESS.equals(detail.getProgress()))
                    .count();
            // 总步骤
            double totalStep = steps.size();
            // 计算进度百分比（取整、进度小于10时默认10）
            long ratio = Math.max(Math.round((stepSuccessNum / totalStep) * 100), 10);
            taskProgress.setRatio(ratio);
        }
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateFailureProgress(String taskId) {
        appTemplateTaskProgressDetailService.update(Wrappers.<JvsAppTemplateTaskProgressDetail>lambdaUpdate()
                .set(JvsAppTemplateTaskProgressDetail::getProgress, AppTemplateTaskProgressEnum.FAILURE)
                .eq(JvsAppTemplateTaskProgressDetail::getTaskId, taskId)
                .eq(JvsAppTemplateTaskProgressDetail::getProgress, AppTemplateTaskProgressEnum.PROCESSING));
    }
}
