package cn.bctools.design.project.service;

import cn.bctools.design.project.dto.AppTemplateTaskProgressResDto;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressDetailEnum;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author hrl
 */
public interface JvsAppTemplateTaskProgressService extends IService<JvsAppTemplateTaskProgress> {

    /**
     * 变更日志
     *
     * @param taskId      任务id
     * @param taskLogEnum 任务日志枚举
     * @param progress    进度
     */
    void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress);

    /**
     * 变更日志
     *
     * @param taskId      任务id
     * @param taskLogEnum 任务日志枚举
     * @param progress    任务日志状态枚举
     * @param duration    耗时(ms)
     */
    void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress, Long duration);

    /**
     * 变更日志
     *
     * @param taskId              任务id
     * @param taskLogEnum         任务日志枚举
     * @param progress            任务日志状态枚举
     * @param duration            耗时(ms)
     * @param exceptionStackTrace 异常栈
     */
    void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress, Long duration, String exceptionStackTrace);

    /**
     * 增加日志
     *
     * @param taskId      任务id
     * @param taskLogEnum 任务日志枚举
     * @param progress    任务日志状态枚举
     * @param duration    耗时(ms)
     * @param content     内容
     */
    void addProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress, Long duration, String content);

    /**
     * 任务结束
     *
     * @param taskId   任务id
     * @param progress 任务状态
     */
    void end(String taskId, AppTemplateTaskProgressEnum progress);


    /**
     * 计算进度
     *
     * @param templateTaskProgressesDto 任务
     */
    void calculateProgress(List<AppTemplateTaskProgressResDto> templateTaskProgressesDto);

    /**
     * 修改异常进度
     *
     * @param taskId 任务id
     */
    void updateFailureProgress(String taskId);
}
