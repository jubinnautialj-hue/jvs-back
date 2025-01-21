package cn.bctools.design.project.service;

import cn.bctools.design.project.dto.AppTemplateTaskProgressResDto;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author hrl
 */
public interface JvsAppTemplateTaskProgressService extends IService<JvsAppTemplateTaskProgress> {

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
}
