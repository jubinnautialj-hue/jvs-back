package cn.bctools.design.project.handler;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.constant.AppConstant;
import cn.bctools.design.crud.entity.*;
import cn.bctools.design.crud.service.CrudPageService;
import cn.bctools.design.crud.service.FormService;
import cn.bctools.design.data.entity.DataEventPo;
import cn.bctools.design.data.entity.DataFieldPo;
import cn.bctools.design.data.entity.DataModelPo;
import cn.bctools.design.external.entity.ExternalPage;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.service.AppMenuService;
import cn.bctools.design.notice.entity.DataNoticePo;
import cn.bctools.design.project.entity.*;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressDetailEnum;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import cn.bctools.design.project.entity.enums.AppVersionStatusEnum;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.service.*;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowDesignVersion;
import cn.bctools.design.workflow.entity.FlowPurview;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 升级旧版轻应用支持版本功能
 */
@Slf4j
@Component
@AllArgsConstructor
public class UpgradeFeatureVersionHandler {
    private static final RedisUtils redisUtils = SpringContextUtil.getBean(RedisUtils.class);
    private final static String LOCK = "upgrade:feature:lock";
    private final static Integer LOCK_TIME = 86400 * 2;
    private final JvsAppService jvsAppService;
    private final JvsAppTemplateService templateService;
    private final JvsAppVersionService appVersionService;
    private final AppTemplateTaskProgressHandler templateTaskProgressHandler;
    private final JvsAppVersionMappingService appVersionMappingService;
    private final AppTemplateDataService<FormPo> formTemplateService;
    private final AppTemplateDataService<AppUrlPo> appUrlTemplateService;
    private final AppTemplateDataService<CrudPage> crudPageTemplateService;
    private final AppTemplateDataService<FunctionBusinessPo> functionBusinessTemplateService;
    private final AppTemplateDataService<FlowDesign> flowDesignTemplateService;
    private final AppTemplateDataService<FlowDesignVersion> flowDesignVersionTemplateService;
    private final AppTemplateDataService<FlowPurview> flowPurviewTemplateService;
    private final AppTemplateDataService<RuleDesignPo> ruleDesignTemplateService;
    private final AppTemplateDataService<DataFieldPo> dataFieldTemplateService;
    private final AppTemplateDataService<DataModelPo> dataModelTemplateService;
    private final AppTemplateDataService<CrudAssociationPo> associationTemplateService;
    private final AppTemplateDataService<DataEventPo> dataEventTemplateService;
    private final AppTemplateDataService<DataNoticePo> dataNoticeTemplateService;
    private final AppTemplateDataService<PrintTemplate> printTemplateTemplateService;
    private final AppTemplateDataService<ExternalPage> externalPageTemplateService;
    private final AppTemplateDataService<AppMenuType> appMenuTypeTemplateService;
    private final AppTemplateDataService<AppMenu> appMenuTemplateService;
    ;
    private final AppTemplateDataService<Identification> identificationTemplateService;
    private final AppMenuService appMenuService;
    private final FormService formService;
    private final CrudPageService crudPageService;

    /**
     * 应用功能升级支持版本
     *
     * @param appId 待升级版本功能的应用id
     */
    @Transactional(rollbackFor = Exception.class)
    public void upgrade(String appId) {
        // 启用轻应用版本功能
        JvsApp jvsApp = Optional.ofNullable(jvsAppService.getById(appId)).orElseThrow(() -> new BusinessException("应用不存在"));
        // 一个应用只允许执行一次升级轻应用版本功能，已升级的不能再升级
        if (jvsApp.getEnableVersionFeature()) {
            throw new BusinessException("已升级轻应用版本功能");
        }
        if (appVersionService.existsVersion(appId)) {
            throw new BusinessException("已升级轻应用版本功能");
        }

        // 避免同时升级
        tryLock(appId);
        // 升级
        SpringContextUtil.getBean(UpgradeFeatureVersionHandler.class).asyncUpgrade(jvsApp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Async
    public void asyncUpgrade(JvsApp jvsApp) {
        String appId = jvsApp.getId();
        try {
            // 初始化任务进度
            String lockKey = getLockKey(appId);
            JvsAppTemplateTaskProgress templateTaskProgress = templateTaskProgressHandler.initTask(lockKey, "升级轻应用版本功能(" + jvsApp.getName() + ")", appId);
            templateTaskProgressHandler.addProgress(templateTaskProgress.getId(), AppTemplateTaskProgressDetailEnum.SUMMARY, AppTemplateTaskProgressEnum.SUCCESS, 0L, "准备升级轻应用版本功能");

            // 保存开发设计版本的设计资源
            templateTaskProgressHandler.addProgress(templateTaskProgress.getId(), AppTemplateTaskProgressDetailEnum.UNIFIED_PERMISSION, AppTemplateTaskProgressEnum.PROCESSING);
            templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.UNIFIED_PERMISSION, () -> {
                saveAppPermission(appId);
            });

            // 初始化版本
            if (jvsApp.getIsDeploy()) {
                // 若应用已发布，则需发布正式模板，并迁移数据
                initGaVersion(templateTaskProgress, jvsApp);
                jvsApp.setUseVersion(AppConstant.DEFAULT_INIT_APP_VERSION);
            } else {
                // 生成开发版
                initDevVersion(templateTaskProgress, jvsApp);
            }

            // 设置启用轻应用版本功能
            jvsApp.setEnableVersionFeature(Boolean.TRUE);
            jvsAppService.updateById(jvsApp);

            // 设置任务结束
            templateTaskProgressHandler.processEnd(templateTaskProgress);
        } catch (Exception e) {
            log.error("升级轻应用版本功能异常：", e);
            throw new BusinessException("升级轻应用版本功能失败", e.getMessage());
        } finally {
            unLock(appId);
        }
    }

    /**
     * 避免同时升级
     *
     * @param appId 应用id
     */
    private static void tryLock(String appId) {
        String lockKey = getLockKey(appId);
        boolean lock = redisUtils.tryLock(lockKey, LOCK_TIME);
        if (Boolean.FALSE.equals(lock)) {
            String msg = "正在升级轻应用版本功能,不能重复执行功能升级操作";
            throw new BusinessException(msg);
        }
    }

    private static void unLock(String appId) {
        String lockKey = getLockKey(appId);
        redisUtils.unLock(lockKey);
    }


    /**
     * 获取轻应用功能升级锁key
     *
     * @param appId 应用id
     * @return 锁key
     */
    private static String getLockKey(String appId) {
        return SysConstant.redisKey(LOCK, appId);
    }

    /**
     * 保存应用的设计资源
     *
     * @param appId 应用id
     */
    public void saveAppPermission(String appId) {
        // 保存列表资源
        List<CrudPage> crudPages = crudPageService.list(Wrappers.<CrudPage>lambdaQuery().eq(CrudPage::getJvsAppId, appId));
        appMenuService.saveBatchPermission(appId, crudPages);
        // 保存表单资源
        List<FormPo> formPos = formService.list(Wrappers.<FormPo>lambdaQuery().eq(FormPo::getJvsAppId, appId));
        appMenuService.saveBatchPermission(appId, formPos);
    }

    /**
     * 初始化开发版
     *
     * @param jvsApp 待发布的应用
     */
    private void initDevVersion(JvsAppTemplateTaskProgress templateTaskProgress, JvsApp jvsApp) {
        String taskProgressId = templateTaskProgress.getId();
        templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.PACK, AppTemplateTaskProgressEnum.WAIT, null, "升级");
        // 初始化开发版
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.PACK, () -> {
            templateService.initVersionDevTemplate(jvsApp);
        });


    }

    /**
     * 初始化正式版，并迁移数据
     * <p>
     * 将原应用所有设计视为正式版本数据，生成正式版本以及正式版本的映射id，以达到数据自动迁移的目的;
     * 然后为此正式版本反向生成来源应用设计id，构造版本迭代需要的数据;
     *
     * @param jvsApp 待发布的应用
     */
    private void initGaVersion(JvsAppTemplateTaskProgress templateTaskProgress, JvsApp jvsApp) {
        // 生成一个应用id，作为正式版的原始应用id
        String affiliationApp = IdGenerator.get32UUID();
        String taskProgressId = templateTaskProgress.getId();

        // 初始化应用正式版
        templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.PACK, AppTemplateTaskProgressEnum.PROCESSING);
        AtomicReference<JvsAppVersion> gaVersionRef = new AtomicReference<>();
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.PACK, () -> {
            JvsAppVersion gaVersion = initVersionGaTemplate(affiliationApp, jvsApp);
            // 初始化版本id映射关系
            initAppVersionMapping(affiliationApp, jvsApp.getId());

            // 正式版生成之后，再依据正式版本，初始化开发版本
            JvsAppVersion devVersion = new JvsAppVersion()
                    // 开发版的应用id与所属应用唯一标识相同
                    .setJvsAppId(affiliationApp)
                    .setAffiliationApp(affiliationApp)
                    .setDescription("")
                    .setVersionType(AppVersionTypeEnum.DEV)
                    .setAppVersion(AppConstant.DEFAULT_INIT_APP_VERSION)
                    .setVersionStatus(AppVersionStatusEnum.USE);
            appVersionService.saveVersion(devVersion);
            gaVersionRef.set(gaVersion);
        });

        templateService.submitVersion(VersionIterationTypeEnum.SUBMIT_VERSION, templateTaskProgress, gaVersionRef.get(), AppVersionTypeEnum.DEV);
    }


    /**
     * 初始化正式版
     *
     * @param affiliationApp 所属应用唯一标识
     * @param jvsApp         当前应用
     * @return 正式版
     */
    private JvsAppVersion initVersionGaTemplate(String affiliationApp, JvsApp jvsApp) {
        JvsAppTemplate jvsAppTemplate = new JvsAppTemplate()
                .setName(jvsApp.getName())
                .setId(jvsApp.getId())
                .setVersionTemplate(Boolean.TRUE);
        templateService.saveTemplate(jvsAppTemplate);
        JvsAppVersion version = new JvsAppVersion()
                .setJvsAppId(jvsApp.getId())
                .setAffiliationApp(affiliationApp)
                .setDescription("")
                .setVersionType(AppVersionTypeEnum.GA)
                .setAppVersion(AppConstant.DEFAULT_INIT_APP_VERSION)
                .setVersionStatus(AppVersionStatusEnum.USE)
                .setTemplateId(jvsAppTemplate.getId());
        appVersionService.saveVersion(version);
        return version;
    }


    /**
     * 批量保存映射正式版设计id
     *
     * @param affiliationApp 所属应用唯一标识
     * @param appId          当前应用id
     */
    private void initAppVersionMapping(String affiliationApp, String appId) {
        Consumer<AppTemplateDataService<?>> saveBatchMappingConsumer = (appTemplateDataServiceImpl) -> {
            List<String> ids = new ArrayList<>();
            appTemplateDataServiceImpl.list(appId, ids);
            List<JvsAppVersionMapping> jvsAppVersionMappings = ids.stream()
                    .map(designId ->
                            new JvsAppVersionMapping()
                                    .setAffiliationApp(affiliationApp)
                                    .setGaDesignId(designId))
                    .collect(Collectors.toList());
            if (ObjectNull.isNotNull(jvsAppVersionMappings)) {
                appVersionMappingService.saveBatch(jvsAppVersionMappings);
            }
            // 初始化版本号
            appTemplateDataServiceImpl.saveAppVersion(appId, AppConstant.DEFAULT_INIT_APP_VERSION);
        };
        saveBatchMappingConsumer.accept(crudPageTemplateService);
        saveBatchMappingConsumer.accept(formTemplateService);
        saveBatchMappingConsumer.accept(functionBusinessTemplateService);
        saveBatchMappingConsumer.accept(appUrlTemplateService);
        saveBatchMappingConsumer.accept(flowDesignTemplateService);
        saveBatchMappingConsumer.accept(flowDesignVersionTemplateService);
        saveBatchMappingConsumer.accept(flowPurviewTemplateService);
        saveBatchMappingConsumer.accept(ruleDesignTemplateService);
        saveBatchMappingConsumer.accept(dataFieldTemplateService);
        saveBatchMappingConsumer.accept(dataModelTemplateService);
        saveBatchMappingConsumer.accept(associationTemplateService);
        saveBatchMappingConsumer.accept(dataEventTemplateService);
        saveBatchMappingConsumer.accept(dataNoticeTemplateService);
        saveBatchMappingConsumer.accept(printTemplateTemplateService);
        saveBatchMappingConsumer.accept(externalPageTemplateService);
        saveBatchMappingConsumer.accept(appMenuTemplateService);
        saveBatchMappingConsumer.accept(appMenuTypeTemplateService);
        saveBatchMappingConsumer.accept(identificationTemplateService);
    }
}
