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
import cn.bctools.design.project.utils.AppTemplateTaskUtils;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    public void end(String taskId, AppTemplateTaskProgressEnum progress) {
        String appId = AppTemplateTaskUtils.getTaskAppId(taskId);
        JvsAppTemplateTaskProgress progressTask = AppTemplateTaskUtils.getTaskProgress(appId, taskId);
        if (ObjectNull.isNull(progressTask)) {
            return;
        }
        progressTask.setProgress(progress)
                .setUpdateTime(LocalDateTime.now());
        // 持久化任务进度
        save(progressTask);

        // 持久化任务进度详情
        Map<Object, Object> detailMap =  AppTemplateTaskUtils.listDetail(taskId);
        List<JvsAppTemplateTaskProgressDetail> details = detailMap.values()
                .stream()
                .map(detail -> JSON.parseObject((String) detail, JvsAppTemplateTaskProgressDetail.class))
                .peek(detail -> {
                    if (AppTemplateTaskProgressEnum.PROCESSING.equals(detail.getProgress())) {
                        detail.setProgress(AppTemplateTaskProgressEnum.FAILURE)
                                .setTenantId(progressTask.getTenantId());
                    }
                })
                .collect(Collectors.toList());

        appTemplateTaskProgressDetailService.saveBatch(details);

        // 删除缓存
        AppTemplateTaskUtils.removeProgressCache(appId, taskId);
        AppTemplateTaskUtils.removeTaskAppId(taskId);
        AppTemplateTaskUtils.removeUserTaskId(progressTask.getCreateById(), taskId);
        AppTemplateTaskUtils.removeProgressDetail(taskId);
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
            templateTaskProgressDetails = processingTaskIds.stream()
                    .map(taskId ->
                            AppTemplateTaskUtils.listDetail(taskId).values()
                                    .stream()
                                    .map(detail -> JSON.parseObject((String) detail, JvsAppTemplateTaskProgressDetail.class))
                                    .peek(detail -> {
                                        if (AppTemplateTaskProgressEnum.PROCESSING.equals(detail.getProgress())) {
                                            detail.setProgress(AppTemplateTaskProgressEnum.FAILURE);
                                        }
                                    })
                                .collect(Collectors.toList())
                    ).flatMap(Collection::stream)
                    .collect(Collectors.toList());
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
}
