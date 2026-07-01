package cn.bctools.design.project.service.impl.data;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.project.entity.JvsApp;
import cn.bctools.design.project.entity.JvsAppVersion;
import cn.bctools.design.project.entity.TemplateBo;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.service.AppTemplateDataService;
import cn.bctools.design.rule.RuleRunService;
import cn.bctools.design.rule.component.XxlJobComponent;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.design.rule.swagger.SwaggerRuleApiCacheService;
import cn.bctools.design.util.CurrentAppUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板——逻辑引擎
 */
@Service
@AllArgsConstructor
public class RuleDesignAppTemplateDataServiceImpl extends AppTemplateDataBase implements AppTemplateDataService<RuleDesignPo> {
    private final RuleDesignService ruleDesignService;
    private final RuleRunService ruleRunService;
    private final XxlJobComponent xxlJobComponent;
    private final SwaggerRuleApiCacheService swaggerRuleApiCacheService;

    @Override
    public List<RuleDesignPo> list(String jvsAppId, List<String> ids) {
        List<RuleDesignPo> list = ruleDesignService.list(Wrappers.query(new RuleDesignPo().setJvsAppId(jvsAppId)));
        {
            List<String> collect = list.stream()
                    .peek(e ->
                            e.setCreateBy(null).setUpdateBy(null).setCreateTime(null).setUpdateTime(null))
                    .map(RuleDesignPo::getId).collect(Collectors.toList());
            List<String> collect2 = list.stream().map(RuleDesignPo::getSecret).collect(Collectors.toList());
            ids.addAll(collect);
            ids.addAll(collect2);
        }
        return list;
    }

    @Override
    public void save(JvsApp jvsApp, JvsAppVersion targetAppVersion, List<String> existsIds, TemplateBo templateBo, TemplateBo targetVersionTemplateBo) {
        // 删除
        delete(ruleDesignService, existsIds, targetVersionTemplateBo.getRuleDesignPos(), RuleDesignPo::getId);

        List<RuleDesignPo> newRuleDesignPos = templateBo.getRuleDesignPos();

        // 处理定时任务
        // 查询目标应用下所有定时任务逻辑引擎,获取任务id集合
        JvsApp threadLocalApp = CurrentAppUtils.getApp();
        CurrentAppUtils.clear();
        List<Integer> taskIds = ruleDesignService.list(Wrappers.<RuleDesignPo>lambdaQuery()
                        .eq(RuleDesignPo::getJvsAppId, jvsApp.getId()))
                .stream()
                .filter(r -> ObjectNull.isNotNull(r.getTask()) && ObjectNull.isNotNull(r.getTask().getId())).map(r -> r.getTask().getId()).collect(Collectors.toList());
        // 批量停止定时任务
        xxlJobComponent.stopBatch(taskIds);
        // 新的定时任务
        if (ObjectNull.isNull(newRuleDesignPos)) {
            return;
        }
        newRuleDesignPos.forEach(ruleDesignPo -> {
            //1、请求有，库里面没有
            if (ruleDesignPo.getOnTask() & ObjectNull.isNotNull(ruleDesignPo.getTask())) {
                ruleDesignPo.getTask().setId(null);
                // 保存定时任务
                xxlJobComponent.saveOrUpdateJob(ruleDesignPo.getTask(), ruleDesignPo.getOnTask(), jvsApp.getName(), ruleDesignPo.getName(), ruleDesignPo.getSecret());
            }
        });
        CurrentAppUtils.setApp(threadLocalApp);

        // 新增或修改
        newRuleDesignPos.forEach(e -> {
            e.setTenantId(null).setCreateBy(null).setUpdateBy(null).setCreateTime(null).setUpdateTime(null);
            // 设置版本号
            setAppVersion(e, RuleDesignPo::setAppVersion, targetAppVersion);
        });
        ruleRunService.importCheck(newRuleDesignPos);
        ruleDesignService.saveOrUpdateBatch(newRuleDesignPos);

        // 发布逻辑API swagger缓存变更事件
        AppVersionTypeEnum targetVersionType = targetAppVersion.getVersionType();
        swaggerRuleApiCacheService.publishSwaggerRuleApiEvent(targetVersionType, false, jvsApp.getId());
    }

    @Override
    public void saveAppVersion(String appId, String appVersion) {
        ruleDesignService.update(Wrappers.<RuleDesignPo>lambdaUpdate().eq(RuleDesignPo::getJvsAppId, appId).set(RuleDesignPo::getAppVersion, appVersion));
    }
}
