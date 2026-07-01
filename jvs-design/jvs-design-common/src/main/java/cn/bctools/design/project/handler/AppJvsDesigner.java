package cn.bctools.design.project.handler;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.*;
import cn.bctools.design.project.service.*;
import cn.bctools.design.rule.entity.RunLogPo;
import cn.bctools.design.rule.service.RunLogService;
import cn.bctools.design.sqlInjector.MapperMethodHandler;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.function.service.FunctionBusinessService;
import cn.bctools.oss.template.OssTemplate;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Component
@AllArgsConstructor
public class AppJvsDesigner implements IJvsDesigner {
    private final MapperMethodHandler mapperMethodHandler;
    private final OssTemplate ossTemplate;
    private final JvsAppLogService jvsAppLogService;
    private final RunLogService runLogService;
    private final FunctionBusinessService functionBusinessService;
    private final JvsAppVersionService jvsAppVersionService;
    private final JvsAppVersionMappingService jvsAppVersionMappingService;
    private final JvsAppTemplateTaskProgressService progressService;
    private final JvsAppTemplateTaskProgressDetailService progressDetailService;
    private final JvsAppTemplateService appTemplateService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void beforeAppDeleted(String appId) {
        // 删除应用日志
        mapperMethodHandler.deletePhysical(jvsAppLogService, Wrappers.<JvsAppLog>lambdaQuery().eq(JvsAppLog::getJvsAppId, appId));
        // 删除逻辑运行时的Po的日志信息
        mapperMethodHandler.deletePhysical(runLogService, Wrappers.<RunLogPo>lambdaQuery().eq(RunLogPo::getJvsAppId, appId));
        // 删除业务表达式
        mapperMethodHandler.deletePhysical(functionBusinessService, Wrappers.<FunctionBusinessPo>lambdaQuery().eq(FunctionBusinessPo::getJvsAppId, appId));
        // 删除任务迭代进度
        List<JvsAppTemplateTaskProgress> progresses = progressService.list(Wrappers.<JvsAppTemplateTaskProgress>lambdaQuery().eq(JvsAppTemplateTaskProgress::getJvsAppId, appId));
        Set<String> progressTaskIds = progresses.stream().map(JvsAppTemplateTaskProgress::getId).collect(Collectors.toSet());
        if (ObjectNull.isNotNull(progressTaskIds)) {
            mapperMethodHandler.deletePhysical(progressDetailService, Wrappers.<JvsAppTemplateTaskProgressDetail>lambdaQuery().in(JvsAppTemplateTaskProgressDetail::getTaskId, progressTaskIds));
            mapperMethodHandler.deletePhysical(progressService, Wrappers.<JvsAppTemplateTaskProgress>lambdaQuery().eq(JvsAppTemplateTaskProgress::getJvsAppId, appId));
        }
        // 删除应用版本相关数据
        List<JvsAppVersion> jvsAppVersions = jvsAppVersionService.list(Wrappers.<JvsAppVersion>lambdaQuery().eq(JvsAppVersion::getJvsAppId, appId));
        if (ObjectNull.isNotNull(jvsAppVersions)) {
            // 删除版本模板文件
            List<String> templateIds = jvsAppVersions.stream().map(JvsAppVersion::getTemplateId).collect(Collectors.toList());
            List<JvsAppTemplate> jvsAppTemplates = appTemplateService.listByIds(templateIds);
            jvsAppTemplates.forEach(jvsAppTemplate ->  ossTemplate.removeFile(jvsAppTemplate.getBucketName(), jvsAppTemplate.getFileName()));
            // 删除版本模板
            mapperMethodHandler.deletePhysical(appTemplateService, Wrappers.<JvsAppTemplate>lambdaQuery().in(JvsAppTemplate::getId, templateIds));
            // 删除版本
            mapperMethodHandler.deletePhysical(jvsAppVersionService, Wrappers.<JvsAppVersion>lambdaQuery().eq(JvsAppVersion::getJvsAppId, appId));
            String affiliationApp = jvsAppVersions.get(0).getAffiliationApp();
            mapperMethodHandler.deletePhysical(jvsAppVersionMappingService, Wrappers.<JvsAppVersionMapping>lambdaQuery().eq(JvsAppVersionMapping::getAffiliationApp, affiliationApp));
        }

    }
}
