package cn.bctools.design.project.service.impl;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.*;
import cn.bctools.common.utils.function.Get;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.constant.AppConstant;
import cn.bctools.design.crud.entity.*;
import cn.bctools.design.data.entity.*;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.external.entity.ExternalPage;
import cn.bctools.design.identification.entity.Identification;
import cn.bctools.design.identification.service.IdentificationService;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.menu.entity.AppMenuType;
import cn.bctools.design.menu.util.DesignPermissionUtil;
import cn.bctools.design.notice.entity.DataNoticePo;
import cn.bctools.design.project.dto.AppVersionSubmitBetaDto;
import cn.bctools.design.project.dto.SwitchModeDto;
import cn.bctools.design.project.dto.VersionIterationBaseDto;
import cn.bctools.design.project.entity.*;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressDetailEnum;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import cn.bctools.design.project.entity.enums.AppVersionStatusEnum;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.project.enums.VersionIterationTypeEnum;
import cn.bctools.design.project.handler.AppTemplateTaskProgressHandler;
import cn.bctools.design.project.mapper.JvsAppTemplateDataMapper;
import cn.bctools.design.project.mapper.JvsAppTemplateMapper;
import cn.bctools.design.project.service.*;
import cn.bctools.design.project.utils.JvsAppTemplateDataIdUtils;
import cn.bctools.design.project.utils.JvsAppTemplateUtils;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.entity.FlowDesignVersion;
import cn.bctools.design.workflow.entity.FlowPurview;
import cn.bctools.function.entity.po.FunctionBusinessPo;
import cn.bctools.message.push.api.InsideNotificationApi;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Auto Generator
 */
@Slf4j
@Service
@AllArgsConstructor
public class JvsAppTemplateServiceImpl extends ServiceImpl<JvsAppTemplateMapper, JvsAppTemplate> implements JvsAppTemplateService {
    JvsAppTemplateDataMapper templateDataMapper;
    JvsAppService jvsAppService;
    RedisUtils redisUtils;
    InsideNotificationApi insideNotificationApi;
    JvsAppVersionMappingService appVersionMappingService;
    IdentificationService identificationService;
    OssTemplate ossTemplate;
    AppTemplateTaskProgressHandler templateTaskProgressHandler;
    JvsAppVersionService appVersionService;
    AppTemplateDataService<FormPo> formTemplateService;
    AppTemplateDataService<AppUrlPo> appUrlTemplateService;
    AppTemplateDataService<CrudPage> crudPageTemplateService;
    AppTemplateDataService<FunctionBusinessPo> functionBusinessTemplateService;
    AppTemplateDataService<FlowDesign> flowDesignTemplateService;
    AppTemplateDataService<FlowDesignVersion> flowDesignVersionTemplateService;
    AppTemplateDataService<FlowPurview> flowPurviewTemplateService;
    AppTemplateDataService<RuleDesignPo> ruleDesignTemplateService;
    AppTemplateDataService<DataFieldPo> dataFieldTemplateService;
    AppTemplateDataService<DataModelPo> dataModelTemplateService;
    AppTemplateDataService<DynamicDataPo> dynamicDataTemplateService;
    AppTemplateDataService<CrudAssociationPo> associationTemplateService;
    AppTemplateDataService<DataEventPo> dataEventTemplateService;
    AppTemplateDataService<DataNoticePo> dataNoticeTemplateService;
    AppTemplateDataService<PrintTemplate> printTemplateTemplateService;
    AppTemplateDataService<ExternalPage> externalPageTemplateService;
    AppTemplateDataService<AppMenuType> appMenuTypeTemplateService;
    AppTemplateDataService<AppMenu> appMenuTemplateService;
    AppTemplateDataService<Identification> identificationTemplateService;
    AppTemplateDataService<DataIdPo> dataIdTemplateService;

    /**
     * key：DES模式下，key必须为8位
     */
    static String key = "jvs1jvs2";
    /**
     * iv：偏移量，ECB模式不需要，CBC模式下必须为8位
     */
    static String iv = "12345678";

    static DES des = new DES(Mode.CBC, Padding.PKCS5Padding, key.getBytes(), iv.getBytes());

    private static final String LOCK_KEY = "template:app:progress:lock";
    private final static Integer LOCK_TIME = 86400;

    /**
     * 应用模板桶
     */
    private static final String OSS_BUCKET_NAME = "jvs-app-template";

    private static final String OSS_BUCKET_NAME_PATH = "template/";

    /**
     * 纯数字id
     */
    private static final Pattern NUMBER_ID_PATTERN = Pattern.compile("\\b\\d{10,20}\\b");
    /**
     * uuid
     */
    private static final Pattern ALPHA_NUM_ID_PATTERN = Pattern.compile("\\b[a-zA-Z0-9]{32,40}\\b");

    private static ThreadPoolExecutor executor = null;

    private ThreadPoolExecutor getExecutor() {
        if (executor == null || executor.isShutdown()) {
            int maxThreads = Runtime.getRuntime().availableProcessors();
            maxThreads = maxThreads - 1 <= 0 ? 1 : maxThreads - 1;
            executor = new ThreadPoolExecutor(
                    maxThreads,
                    maxThreads,
                    10L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingDeque<>(),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.AbortPolicy());
        }
        return executor;
    }

    /**
     * 执行版本迭代的模板方法
     * <p>
     * 模板创建应用、版本迭代等操作必须调用此方法
     *
     * @param lockKey       锁key
     * @param businessLogic 业务逻辑
     */
    private void executeTemplate(String lockKey, Runnable businessLogic) {
        try {
            businessLogic.run();
        } catch (Exception e) {
            log.error("模板创建应用或版本迭代异常：", e);
            throw new BusinessException("模板创建应用或版本迭代失败", e.getMessage());
        } finally {
            redisUtils.unLock(lockKey);
            templateTaskProgressHandler.removeHeartAndCache(lockKey, null);
        }
    }

    /**
     * 获取应用模板
     *
     * @param templateId 模板id
     * @return 应用模板
     */
    private JvsAppTemplate getAppTemplate(String templateId) {
        //区别本地和服务器处理逻辑
        JvsAppTemplate template = this.getById(templateId);
        if (ObjectNull.isNull(template)) {
            //如果本地没有，说明不是模板文件上传的，通过在线获取模板信息
            Object data = Optional.ofNullable(JvsAppTemplateUtils.get(HttpUtil.createGet(JvsAppTemplateUtils.DOMAIN + "/JvsAppTemplate/data/" + templateId)))
                    .map(R::getData)
                    .orElse(null);
            if (ObjectNull.isNull(data)) {
                log.info("应用模板不存在, 模板id: {}, ", templateId);
                throw new BusinessException("该模板不存在请重试");
            }
            //将返回的数据进行转换
            try {
                template = JSONObject.parseObject(JSONObject.toJSONString(data), JvsAppTemplate.class);
            } catch (Exception e) {
                //如果是收费应用
                throw new BusinessException("该模板异常请重试");
            }
            if (!template.getFree()) {
                throw new BusinessException("付费应用请联系商务安装");
            }
        } else {
            //不是空，走本地，是空走远程
            template.setData(getData(template));
        }
        return template;
    }

    /**
     * 获取轻应用版本迭代锁
     *
     * @param key key
     * @return 锁key
     */
    private static String getLockKey(String key) {
        return SysConstant.redisKey(LOCK_KEY, key);
    }

    /**
     * 校验是否可以通过模板创建应用
     *
     * @param templateTaskProgress 任务进度
     * @param templateId           模板id
     * @return 锁key
     */
    private String checkCanTemplateCreateApp(JvsAppTemplateTaskProgress templateTaskProgress, String templateId) {
        AtomicReference<String> lockRef = new AtomicReference<>();
        templateTaskProgressHandler.addProgress(templateTaskProgress.getId(), AppTemplateTaskProgressDetailEnum.CHECK, AppTemplateTaskProgressEnum.SUCCESS);
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.CHECK, () -> {
            // 加锁
            String lockKey = getLockKey(templateId);
            boolean lock = redisUtils.tryLock(lockKey, LOCK_TIME);
            if (Boolean.FALSE.equals(lock)) {
                throw new BusinessException("该模板正在创建应用请稍后再试");
            }
            lockRef.set(lockKey);
        });
        return lockRef.get();
    }

    /**
     * 校验是否可以迭代
     *
     * @param affiliationApp    所属应用唯一标识
     * @param targetVersionType 目标版本类型
     * @return 锁key
     */
    @Override
    public String checkCanAppIterator(String affiliationApp, AppVersionTypeEnum targetVersionType) {
        // 加锁
        String key = affiliationApp + targetVersionType.getValue();
        String lockKey = getLockKey(key);
        boolean lock = redisUtils.tryLock(lockKey, LOCK_TIME);
        if (Boolean.FALSE.equals(lock)) {
            throw new BusinessException("存在未完成的迭代任务请稍后再试");
        }
        return lockKey;
    }

    /**
     * 校验是否可以迭代
     *
     * @param templateTaskProgress 任务进度
     * @param affiliationApp       所属应用唯一标识
     * @param targetVersionType    目标版本类型
     * @return 锁key
     */
    private String checkCanAppIterator(JvsAppTemplateTaskProgress templateTaskProgress, String affiliationApp, AppVersionTypeEnum targetVersionType) {
        templateTaskProgressHandler.addProgress(templateTaskProgress.getId(), AppTemplateTaskProgressDetailEnum.CHECK, AppTemplateTaskProgressEnum.SUCCESS);
        AtomicReference<String> lockRef = new AtomicReference<>();
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.CHECK, () -> {
            String lockKey = checkCanAppIterator(affiliationApp, targetVersionType);
            lockRef.set(lockKey);
        });
        return lockRef.get();
    }

    /**
     * 模板创建应用
     *
     * @param lockKey              锁
     * @param templateTaskProgress 任务进度id
     * @param templateId           模板id
     */
    private void templateCreateApp(String lockKey, JvsAppTemplateTaskProgress templateTaskProgress, String templateId) {
        // 获取模板对应的版本
        JvsAppVersion sourceVersion = Optional.ofNullable(appVersionService.getByTemplateId(templateId))
                .orElseGet(() -> new JvsAppVersion()
                        .setAppVersion(AppConstant.DEFAULT_INIT_APP_VERSION)
                        .setVersionType(AppVersionTypeEnum.DEV)
                        .setTemplateId(templateId));
        versionIteration(null, templateTaskProgress, sourceVersion, AppVersionTypeEnum.DEV);
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createByTemplateId(String templateId) {
        // 初始化任务进度
        JvsAppTemplate template = getById(templateId);
        String summary = "在线模板生成应用";
        if (ObjectNull.isNotNull(template)) {
            summary = "在线模板" + StringPool.LEFT_BRACKET + template.getName() + StringPool.RIGHT_BRACKET + "生成应用";
        }
        String key = getLockKey(templateId);
        JvsAppTemplateTaskProgress templateTaskProgress = templateTaskProgressHandler.initTask(key, summary, null);
        // 校验是否可以通过模板创建应用
        String lockKey = checkCanTemplateCreateApp(templateTaskProgress, templateId);
        Runnable createApp = () -> {
            // 模板创建应用
            templateCreateApp(lockKey, templateTaskProgress, templateId);
            // 设置任务结束
            templateTaskProgressHandler.processEnd(templateTaskProgress);
        };
        executeTemplate(lockKey, createApp);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitVersion(VersionIterationTypeEnum versionIterationType, JvsAppTemplateTaskProgress templateTaskProgress, JvsAppVersion sourceVersion, AppVersionTypeEnum targetVersionType) {
        // 校验是否可迭代
        String locKey = checkCanAppIterator(templateTaskProgress, sourceVersion.getAffiliationApp(), targetVersionType);
        Runnable iteration = () -> {
            // 版本迭代
            versionIteration(versionIterationType, templateTaskProgress, sourceVersion, targetVersionType);
        };
        executeTemplate(locKey, iteration);
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void asyncSubmitVersion(String lockKey, VersionIterationTypeEnum versionIterationType, JvsAppTemplateTaskProgress templateTaskProgress, JvsAppVersion sourceVersion, AppVersionTypeEnum targetVersionType) {
        Runnable asyncSubmitVersion = () -> {
            // 版本迭代
            versionIteration(versionIterationType, templateTaskProgress, sourceVersion, targetVersionType);
            // 设置任务结束
            templateTaskProgressHandler.processEnd(templateTaskProgress);
        };
        executeTemplate(lockKey, asyncSubmitVersion);
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void uploadTemplateCreateApp(JvsAppTemplate jvsAppTemplate, String fileName) {
        // 初始化任务进度
        jvsAppTemplate.setId(IdGenerator.get32UUID());
        String lockey = getLockKey(jvsAppTemplate.getId());
        JvsAppTemplateTaskProgress templateTaskProgress = templateTaskProgressHandler.initTask(lockey, "导入模板(" + fileName + ")创建应用", jvsAppTemplate.getId());
        String taskProgressId = templateTaskProgress.getId();
        // 导入模板
        templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.PACK, AppTemplateTaskProgressEnum.PROCESSING, null, "导入模板");
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.PACK, () -> {
            saveUploadTemplate(jvsAppTemplate);
        });
        // 校验是否可以通过模板创建应用
        String lockKey = checkCanTemplateCreateApp(templateTaskProgress, jvsAppTemplate.getId());
        Runnable createApp = () -> {
            // 创建应用
            templateCreateApp(lockKey, templateTaskProgress, jvsAppTemplate.getId());
            // 删除模板
            removeById(jvsAppTemplate.getId());
            removeTemplateData(jvsAppTemplate);
            // 设置任务结束
            templateTaskProgressHandler.processEnd(templateTaskProgress);
        };
        executeTemplate(lockKey, createApp);
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitBeta(String lockKey, JvsApp jvsApp, AppVersionSubmitBetaDto dto) {
        Runnable submitBeta = () -> {
            String appId = jvsApp.getId();
            // 初始化任务进度
            JvsAppTemplateTaskProgress templateTaskProgress = templateTaskProgressHandler.initTask(lockKey, "提交测试(" + jvsApp.getName() + ")", appId);
            String taskProgressId = templateTaskProgress.getId();
            templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.SUMMARY, AppTemplateTaskProgressEnum.SUCCESS, null, "版本号：" + dto.getAppVersion());
            // 保存开发版本
            templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.PACK, AppTemplateTaskProgressEnum.PROCESSING);
            AtomicReference<JvsAppVersion> sourceVersionRef = new AtomicReference<>();
            templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.PACK, () -> {
                JvsAppVersion dev = Optional.ofNullable(appVersionService.getVersion(dto.getAppVersion(), AppVersionTypeEnum.DEV, appId)).orElseGet(JvsAppVersion::new)
                        .setJvsAppId(appId)
                        .setAffiliationApp(appId)
                        .setVersionType(AppVersionTypeEnum.DEV)
                        .setAppVersion(dto.getAppVersion())
                        .setDescription(dto.getDescription())
                        .setVersionStatus(AppVersionStatusEnum.USE);
                JvsAppTemplate jvsAppTemplate = Optional.ofNullable(dev.getTemplateId()).map(this::getById).orElseGet(JvsAppTemplate::new);
                String templateId = jvsAppTemplate.getId();
                jvsAppTemplate
                        .setId(appId)
                        .setName(jvsApp.getName())
                        .setLogo(jvsApp.getLogo())
                        .setDescription(jvsApp.getDescription())
                        .setVersionTemplate(Boolean.TRUE)
                        .setDeployData(dto.getDeployData())
                        .setDeployDataModelIds(dto.getDeployDataModelIds());
                saveTemplate(templateId, jvsAppTemplate);
                templateId = jvsAppTemplate.getId();
                saveAppVersion(appId, dev, dev, templateId);
                sourceVersionRef.set(dev);
            });

            // 发布到测试版本
            versionIteration(VersionIterationTypeEnum.SUBMIT_VERSION, templateTaskProgress, sourceVersionRef.get(), AppVersionTypeEnum.BETA);

            // 设置任务结束
            templateTaskProgressHandler.processEnd(templateTaskProgress);
        };
        executeTemplate(lockKey, submitBeta);
    }

    @Async
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitGa(String lockKey, JvsApp jvsApp, JvsAppVersion betaVersion, VersionIterationBaseDto dto) {
        Runnable submitGa = () -> {
            // 初始化任务进度
            JvsAppTemplateTaskProgress templateTaskProgress = templateTaskProgressHandler.initTask(lockKey, "正式发布(" + jvsApp.getName() + ")", betaVersion.getAffiliationApp());
            String taskProgressId = templateTaskProgress.getId();
            templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.SUMMARY, AppTemplateTaskProgressEnum.SUCCESS, null, "版本号：" + betaVersion.getAppVersion());

            // 保存测试版本
            templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.PACK, AppTemplateTaskProgressEnum.PROCESSING);
            AtomicReference<JvsAppVersion> sourceVersionRef = new AtomicReference<>();
            templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.PACK, () -> {
                JvsAppTemplate jvsAppTemplate = getById(betaVersion.getTemplateId());
                String templateId = jvsAppTemplate.getId();
                jvsAppTemplate
                        .setId(jvsApp.getId())
                        .setName(jvsApp.getName())
                        .setLogo(jvsApp.getLogo())
                        .setDescription(jvsApp.getDescription())
                        .setDeployData(dto.getDeployData())
                        .setDeployDataModelIds(dto.getDeployDataModelIds());
                AppVersionTypeEnum targetVersionType = betaVersion.getVersionType();
                SwitchModeDto currentMode = ModeUtils.getSwitchMode();
                ModeUtils.setSwitchModel(new SwitchModeDto().setMode(targetVersionType));
                saveTemplate(templateId, jvsAppTemplate);
                // 恢复模式上下文
                ModeUtils.setSwitchModel(currentMode);
                templateId = jvsAppTemplate.getId();
                saveAppVersion(jvsApp.getId(), betaVersion, betaVersion, templateId);
                sourceVersionRef.set(betaVersion);
            });

            // 发布到正式版本
            versionIteration(VersionIterationTypeEnum.SUBMIT_VERSION, templateTaskProgress, sourceVersionRef.get(), AppVersionTypeEnum.GA);

            // 设置任务结束
            templateTaskProgressHandler.processEnd(templateTaskProgress);
        };
        executeTemplate(lockKey, submitGa);
    }

    /**
     * 获取模板文件名
     *
     * @return 模板文件名
     */
    private Function<JvsAppTemplate, String> templateSummaryFunction() {
        return template -> {
            StringBuilder summaryBuilder = new StringBuilder();
            if (ObjectNull.isNotNull(JvsAppTemplateUtils.getTemplateFileName())) {
                summaryBuilder
                        .append(StringPool.DASH)
                        .append(StringPool.QUOTE)
                        .append(JvsAppTemplateUtils.getTemplateFileName())
                        .append(StringPool.QUOTE);
            }
            return summaryBuilder.toString();
        };
    }

    /**
     * 版本迭代
     *
     * @param versionIterationType 应用版本迭代操作类型
     * @param templateTaskProgress 任务进度
     * @param sourceAppVersion     来源版本
     * @param targetVersionType    目标版本类型
     */
    private void versionIteration(VersionIterationTypeEnum versionIterationType, JvsAppTemplateTaskProgress templateTaskProgress, JvsAppVersion sourceAppVersion, AppVersionTypeEnum targetVersionType) {
        JvsAppTemplateUtils.setContextVersionIterationType(versionIterationType);
        String taskProgressId = templateTaskProgress.getId();
        // 准备创建应用或版本迭代
        AtomicReference<Boolean> typeRef = new AtomicReference<>();
        AtomicReference<JvsAppTemplate> templateRef = new AtomicReference<>();
        String prepareContent = ObjectNull.isNull(versionIterationType) ? "准备创建应用" : "准备应用版本迭代";
        templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.PREPARE, AppTemplateTaskProgressEnum.PROCESSING, null, prepareContent);
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.PREPARE, () -> {
            long startMilli = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now());
            String templateId = sourceAppVersion.getTemplateId();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //区别本地和服务器处理逻辑
            JvsAppTemplate template = Optional.ofNullable(sourceAppVersion.getJvsAppTemplate()).orElseGet(() -> getAppTemplate(templateId));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String templateFiledName = templateSummaryFunction().apply(template);
            if (ObjectNull.isNotNull(templateFiledName)) {
                templateTaskProgressHandler.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.SUMMARY, AppTemplateTaskProgressEnum.SUCCESS, LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()) - startMilli, "模板文件" + templateFiledName);
            }

            // TRUE-模板创建应用， FALSE-版本迭代
            Boolean versionTemplate = template.getVersionTemplate();
            log.info("PREPARE步骤 - template.getVersionTemplate()返回值: {}", versionTemplate);
            boolean type = Boolean.FALSE.equals(Optional.ofNullable(versionTemplate).orElse(Boolean.FALSE));
            log.info("PREPARE步骤 - 计算得到的type值: {} (含义: {} )", type, type ? "模板创建应用" : "版本迭代");
            typeRef.set(type);
            templateRef.set(template);
            log.info("PREPARE步骤 - 已设置 typeRef 和 templateRef");
        });

        // 创建或迭代应用
//        TenantContextHolder.setTenantId(UserCurrentUtils.getCurrentUser().getTenantId());
        // 记录调用前的sourceAppVersion信息
        if (sourceAppVersion != null) {
            log.info("调用createOrIterationApp前 - sourceAppVersion完整信息: {}", JSONObject.toJSONString(sourceAppVersion));
            log.info("调用createOrIterationApp前 - sourceAppVersion.affiliationApp: [{}]", sourceAppVersion.getAffiliationApp());
        } else {
            log.warn("调用createOrIterationApp前 - sourceAppVersion 为 null！");
        }
        
        // 检查typeRef和templateRef是否为null
        Boolean typeValue = typeRef.get();
        JvsAppTemplate templateValue = templateRef.get();
        log.info("调用createOrIterationApp前 - typeRef.get()结果: {}, templateRef.get()是否为null: {}", 
            typeValue, templateValue == null);
        
        if (typeValue == null) {
            log.error("严重错误：typeRef.get() 返回 null！这会导致createOrIterationApp方法的type参数为null");
            throw new BusinessException("type参数为null，无法判断是模板创建应用还是版本迭代");
        }
        
        createOrIterationApp(templateTaskProgress, typeValue, sourceAppVersion, targetVersionType, templateValue);
    }

    /**
     * 创建|迭代应用
     *
     * @param type              TRUE-模板创建应用， FALSE-版本迭代
     * @param sourceAppVersion  来源版本
     * @param targetVersionType 目标版本类型
     * @param template          来源版本模板
     */
    private void createOrIterationApp(JvsAppTemplateTaskProgress templateTaskProgress, boolean type, JvsAppVersion sourceAppVersion, AppVersionTypeEnum targetVersionType, JvsAppTemplate template) {
        String taskProgressId = templateTaskProgress.getId();
        LocalDateTime now = LocalDateTime.now();
        // 初始化模板任务日志
        templateTaskProgressHandler.initCreateOrIterationProgress(taskProgressId);
        // 解密模板内容
        AtomicReference<JvsApp> appRef = new AtomicReference<>(null);
        AtomicReference<String> dataRef = new AtomicReference<>(null);
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.DECRYPT_DATA, () -> {
            // 开始创建应用或迭代应用
            JvsApp jvsApp = JSONObject.parseObject(template.getData(), JvsApp.class);
            appRef.set(jvsApp);
            dataRef.set(des.decryptStr(jvsApp.getData()));
        });

        JvsApp jvsApp = appRef.get();

        // 创建应用或迭代应用
        AtomicReference<JvsAppVersion> targetVersionRef = new AtomicReference<>();
        TemplateBo templateBoSource = JSONObject.parseObject(dataRef.get(), TemplateBo.class);
        
        // 在执行前进行空值检查并记录详细信息
        log.info("ANALYSIS_APP 步骤前的空值检查 - jvsApp: {}, template: {}, sourceAppVersion: {}, templateBoSource: {}, type: {}",
            jvsApp != null, template != null, sourceAppVersion != null, templateBoSource != null, type);
        
        // 如果是版本迭代模式，记录sourceAppVersion的完整信息
        if (!type && sourceAppVersion != null) {
            log.info("版本迭代模式 - sourceAppVersion完整信息: {}", JSONObject.toJSONString(sourceAppVersion));
            log.info("版本迭代模式 - affiliationApp字段值: [{}], 是否为null: {}, 是否为空字符串: {}",
                sourceAppVersion.getAffiliationApp(),
                sourceAppVersion.getAffiliationApp() == null,
                sourceAppVersion.getAffiliationApp() != null && sourceAppVersion.getAffiliationApp().isEmpty());
        }
        
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.ANALYSIS_APP, () -> {
            // 在 lambda 内部再次检查关键对象
            if (jvsApp == null) {
                throw new BusinessException("ANALYSIS_APP 步骤中 jvsApp 为 null");
            }
            if (template == null) {
                throw new BusinessException("ANALYSIS_APP 步骤中 template 为 null");
            }
            if (!type && sourceAppVersion == null) {
                throw new BusinessException("ANALYSIS_APP 步骤中 sourceAppVersion 为 null，当 type 为 false 时");
            }
            if (templateBoSource == null) {
                throw new BusinessException("ANALYSIS_APP 步骤中 templateBoSource 为 null");
            }
            if (now == null) {
                throw new BusinessException("ANALYSIS_APP 步骤中 now 为 null");
            }
            
            jvsApp.setCreateTime(now);
            String affiliationAppId = null;
            JvsAppVersion targetVersion = null;
            if (type) {
                createJvsApp(template, jvsApp);
                // 模板创建应用, 以新应用id作为所属应用唯一标识
                affiliationAppId = jvsApp.getId();
                targetVersion = new JvsAppVersion().setAppVersion(AppConstant.DEFAULT_INIT_APP_VERSION).setVersionType(targetVersionType).setAffiliationApp(affiliationAppId);
            } else {
                // 版本迭代, 直接获取来源版本的所属应用唯一标识
                log.info("版本迭代模式 - sourceAppVersion详情: id={}, appVersion={}, affiliationApp={}, versionType={}",
                    sourceAppVersion.getId(),
                    sourceAppVersion.getAppVersion(),
                    sourceAppVersion.getAffiliationApp(),
                    sourceAppVersion.getVersionType());
                
                if (sourceAppVersion.getAffiliationApp() == null || sourceAppVersion.getAffiliationApp().trim().isEmpty()) {
                    throw new BusinessException("版本迭代失败：来源版本的所属应用ID(affiliationApp)为空，请检查来源版本数据是否完整。来源版本ID: " + sourceAppVersion.getId());
                }
                affiliationAppId = sourceAppVersion.getAffiliationApp();
                
                if (templateBoSource.getIdentifiers() == null) {
                    log.warn("ANALYSIS_APP 步骤中 templateBoSource.getIdentifiers() 为 null");
                }
                
                // 若有自定义标识，需要校验标识在当前租户下是否已存在，若已存在，则校验是否属于当前应用或其派生应用，若不是，则不允许迭代
                checkCanIterationApp(affiliationAppId, templateBoSource.getIdentifiers());

                // 版本查询 - 增加详细日志
                log.info("准备查询目标版本 - appVersion: {}, targetVersionType: {}, affiliationAppId: {}",
                    sourceAppVersion.getAppVersion(), targetVersionType, affiliationAppId);

                targetVersion = appVersionService.getVersion(sourceAppVersion.getAppVersion(), targetVersionType, affiliationAppId);

                log.info("版本查询结果 - targetVersion是否为null: {}", targetVersion == null);

                if (ObjectNull.isNull(targetVersion)) {
                    log.info("目标版本不存在，准备创建新版本 - appVersion: {}, versionType: {}, affiliationApp: {}",
                        sourceAppVersion.getAppVersion(), targetVersionType, affiliationAppId);
                    targetVersion = new JvsAppVersion()
                        .setAppVersion(sourceAppVersion.getAppVersion())
                        .setVersionType(targetVersionType)
                        .setAffiliationApp(affiliationAppId);
                    log.info("新版本对象创建完成 - targetVersion: {}", JSONObject.toJSONString(targetVersion));
                } else {
                    log.info("找到已存在的目标版本 - targetVersion.id: {}, targetVersion.appVersion: {}",
                        targetVersion.getId(), targetVersion.getAppVersion());
                }

                // 在调用iterationJvsApp前检查关键字段
                log.info("准备调用iterationJvsApp - template.id: {}, jvsApp.id: {}, targetVersion.affiliationApp: {}",
                    template != null ? template.getId() : "null",
                    jvsApp != null ? jvsApp.getId() : "null",
                    targetVersion != null ? targetVersion.getAffiliationApp() : "null");

                iterationJvsApp(template, jvsApp, targetVersion);
            }
            targetVersionRef.set(targetVersion);
            // 更新应用ID：只在版本迭代模式下执行
            if (!type) {
                String oldAffiliationAppId = sourceAppVersion.getAffiliationApp();
                if (oldAffiliationAppId != null) {
                    templateTaskProgressHandler.updateAppId(taskProgressId, affiliationAppId, oldAffiliationAppId);
                } else {
                    log.warn("ANALYSIS_APP 步骤 - sourceAppVersion.getAffiliationApp() 为null，跳过updateAppId操作");
                }
            }
        });

        // 解析模板数据
        AtomicReference<TemplateBo> templateRef = new AtomicReference<>(null);
        // 目标版本已存在映射的设计id集合
        List<String> existsIds = new ArrayList<>();
        // 新的设计id映射关系
        List<JvsAppVersionMapping> newVersionMappings = new ArrayList<>();
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.ANALYSIS_DATA, () -> {
            // 增加保护性检查，防止targetVersionRef为null
            JvsAppVersion targetVersion = targetVersionRef.get();
            if (targetVersion == null) {
                log.error("ANALYSIS_DATA步骤严重错误：targetVersionRef.get()返回null，这可能是并发问题或版本创建失败");
                throw new BusinessException("目标版本未正确初始化，请检查版本创建逻辑");
            }

            List<String> ids = templateBoSource.getIds().stream().filter(ObjectNull::isNotNull).collect(Collectors.toList());
            // 获取应用版本设计id映射集合
            String affiliationAppId = targetVersion.getAffiliationApp();
            List<JvsAppVersionMapping> appVersionMappings = appVersionMappingService.getIdMappings(affiliationAppId);
            // 模板数据分片处理
            // Map<待替换的id, 新id>
            Map<String, String> idReplaceMapping = new HashMap<>();
            String appId = jvsApp.getId();
            String oldJvsAppId = ids.get(0);
            idReplaceMapping.put(oldJvsAppId, appId);
            for (int i = 1; i < ids.size(); i++) {
                String currentId = ids.get(i);
                if (currentId.length() < 10) {
                    throw new BusinessException("模板文件已损坏请重新生成模板");
                }
                // 根据设计id找到映射设计id
                JvsAppVersionMapping idMapping = getIdMappingDetail(affiliationAppId, currentId, sourceAppVersion.getVersionType(), appVersionMappings);
                String idStr = getMappingId(targetVersionType, idMapping);
                // 已存在映射的设计id：使用该设计id
                // 不存在映射的设计id：生成新id
                if (ObjectNull.isNull(idStr)) {
                    idStr = JvsAppTemplateDataIdUtils.getNewId(Boolean.TRUE, currentId, templateBoSource);
                    // 设置id映射关系
                    // 若是版本迭代，且没有来源设计id, 且目标版本不是开发版本， 则是第一次发布版本，设置来源设计id
                    // 若目标版本是开发版本，则设置新的id作为来源设计id（因为在旧应用升级版本功能时，可能会以旧应用的设计作为正式版本，需要反向生成id映射关系）
                    if (Boolean.FALSE.equals(type) && ObjectNull.isNull(idMapping.getSourceDesignId()) && Boolean.FALSE.equals(AppVersionTypeEnum.DEV.equals(targetVersionType))) {
                        setIdMapping(currentId, AppVersionTypeEnum.DEV, idMapping);
                    }
                    setIdMapping(idStr, targetVersionType, idMapping);
                    newVersionMappings.add(idMapping);
                } else {
                    existsIds.add(idStr);
                }
                String idString = idStr;
                idReplaceMapping.put(currentId, idString);
            }
            // 替换id
            templateBoSource.setIds(null);
            String data = beanToJsonString(templateBoSource);
            Map<Integer, String> stringMap = splitDataString(data);
            parallelReplaceId(stringMap, idReplaceMapping);
            // 将处理后的分段数据按顺序拼接为完整的字符串
            data = stringMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).collect(Collectors.joining());
            stringMap.clear();

            // 向上兼容(兼容2.1.7的模板，转为2.1.8支持的数据结构)
            data = upwardCompatibilityMenu(jvsApp.getId(), template, data);
            TemplateBo templateBo = stringToBean(data, TemplateBo.class);
            List<String> newIds = new ArrayList<>(idReplaceMapping.values());
            newIds.remove(appId);
            newIds.add(0, appId);
            templateBo.setIds(newIds);
            templateRef.set(templateBo);
        });

        // 保存
        String appId = jvsApp.getId();
        TemplateBo templateBo = templateRef.get();

        // 模板创建应用处理
        if (type) {
            // 若有自定义标识，需要校验标识在当前租户下是否已存在，若已存在，则清空模板中的标识
            checkCreateApp(templateBo);
        }

        JvsAppVersion targetVersion = targetVersionRef.get();
        // 增加日志记录targetVersion的关键字段
        log.info("准备获取目标版本模板 - targetVersion详情: id={}, appVersion={}, versionType={}, affiliationApp={}, templateId={}, jvsAppId={}",
            targetVersion != null ? targetVersion.getId() : null,
            targetVersion != null ? targetVersion.getAppVersion() : null,
            targetVersion != null ? targetVersion.getVersionType() : null,
            targetVersion != null ? targetVersion.getAffiliationApp() : null,
            targetVersion != null ? targetVersion.getTemplateId() : null,
            targetVersion != null ? targetVersion.getJvsAppId() : null);
        // 获取指定版本号的设计模板
        TemplateBo targetVersionTemplateBo = getTargetVersionTemplate(targetVersion);
        log.info("获取目标版本模板完成 - targetVersionTemplateBo是否为null: {}", targetVersionTemplateBo == null);
        // 表单
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.FORM, () -> {
            formTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
            associationTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
            dataEventTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
            printTemplateTemplateService.save(jvsApp, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 列表
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.PAGE, () -> {
            crudPageTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 逻辑引擎
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.RULE, () -> {
            functionBusinessTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
            ruleDesignTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 流程设计
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.FLOW, () -> {
            flowDesignVersionTemplateService.save(jvsApp, existsIds, templateBo, targetVersionTemplateBo);
            flowDesignTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
            flowPurviewTemplateService.save(jvsApp, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 自定义页面
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.APP_URL, () -> {
            appUrlTemplateService.save(jvsApp, existsIds, templateBo, targetVersionTemplateBo);
            externalPageTemplateService.save(jvsApp, existsIds, templateBo, targetVersionTemplateBo);
            identificationTemplateService.save(jvsApp, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 消息通知
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.DATA_NOTICE, () -> {
            dataNoticeTemplateService.save(jvsApp, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 创建目录
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.MENU, () -> {
            appMenuTypeTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
            appMenuTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 数据模型
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.DATA_MODEL, () -> {
            dataModelTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 数据模型字段
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.DATA_MODEL_FIELD, () -> {
            dataFieldTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
        });
        // 模型数据
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.DATA, () -> {
            dynamicDataTemplateService.save(jvsApp, targetVersion, existsIds, templateBo, targetVersionTemplateBo);
            dataIdTemplateService.save(null, templateBo, null);
        });
        // 应用版本
        templateTaskProgressHandler.runTask(templateTaskProgress, AppTemplateTaskProgressDetailEnum.APP_VERSION, () -> {
            // 保存新的id关系映射
            saveVersionMapping(newVersionMappings, templateBo);
            // 保存新的版本
            saveVersionTemplate(appId, sourceAppVersion, targetVersion, template, templateBo);
        });
        // 发送消息
        sendCreateAppNotice(type, jvsApp);
    }

    /**
     * 多线程替换id，并等待所有线程执行结束
     *
     * @param stringMap 待替换id的分段数据
     * @param idMapping 待替换的id映射
     */
    @SneakyThrows
    private void parallelReplaceId(Map<Integer, String> stringMap, Map<String, String> idMapping) {
        List<CompletableFuture<Void>> futures = stringMap.entrySet()
                .stream()
                .map(e ->
                        CompletableFuture.runAsync(() -> {
                            Set<String> repIds = new HashSet<>();
                            findIds(e.getValue(), NUMBER_ID_PATTERN, repIds);
                            findIds(e.getValue(), ALPHA_NUM_ID_PATTERN, repIds);
                            for (String repId : repIds) {
                                String rpId = idMapping.get(repId);
                                if (ObjectNull.isNotNull(rpId)) {
                                    e.setValue(StringUtils.replace(e.getValue(), repId, rpId));
                                }
                            }
                        }, getExecutor()))
                .collect(Collectors.toList());
        CompletableFuture<Void> allof = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allof.get();
        // 关闭线程池
        getExecutor().shutdown();
    }

    /**
     * 提取id
     *
     * @param str     待提取id字符串
     * @param pattern 提取id正则
     * @param ids     字符串中提取的id集合
     */
    private static void findIds(String str, Pattern pattern, Set<String> ids) {
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            ids.add(matcher.group());
        }
    }


    /**
     * 拆分数据字符串
     *
     * @param dataStr 完整的数据字符串
     * @return Map<序号, 子字符串>
     */
    private Map<Integer, String> splitDataString(String dataStr) {
        Map<Integer, String> stringMap = new HashMap<>(8);
        int maxLength = dataStr.length();
        int offset = 50000;
        int lastIndexOf = 0;
        int num = 1;
        boolean end = Boolean.TRUE;
        while (end) {
            int formIndex = lastIndexOf + offset;
            int indexOf = formIndex >= maxLength ? maxLength : dataStr.indexOf("}", formIndex);
            if (indexOf >= maxLength) {
                end = false;
            }
            indexOf = Math.min((indexOf + 1), maxLength);
            String splitStr = dataStr.substring(lastIndexOf, indexOf);
            stringMap.put(num, splitStr);
            lastIndexOf = indexOf;
            num++;
        }
        log.debug("模板数据总字符数：{}, 共分为{}片", maxLength, stringMap.size());
        return stringMap;
    }

    /**
     * 创建应用
     *
     * @param template 模板
     * @param jvsApp   应用信息
     */
    private void createJvsApp(JvsAppTemplate template, JvsApp jvsApp) {
        initApp(template, jvsApp);
        String newJvsAppId = IdGenerator.get32UUID();
        jvsApp.setId(newJvsAppId);
        jvsApp.setCreateBy(null);
        jvsApp.setCreateById(null);
        jvsApp.setUpdateBy(null);
        // 设置默认权限
        jvsApp.setDefaultRole();
        // 创建应用时，默认不是推荐应用
        jvsApp.setRecommend(false);
        jvsAppService.save(jvsApp);
    }

    /**
     * 应用迭代
     *
     * @param jvsApp        应用
     * @param targetVersion 目标版本
     */
    private void iterationJvsApp(JvsAppTemplate template, JvsApp jvsApp, JvsAppVersion targetVersion) {
        initApp(template, jvsApp);
        // 版本迭代
        String appId = targetVersion.getJvsAppId();
        if (ObjectNull.isNull(appId)) {
            // 获取目标版本对应的应用id， 若目标版本的应用id不存在，则生成新的应用id
            appId = appVersionService.getVersionTypeAppId(targetVersion.getAffiliationApp(), targetVersion.getVersionType());
            if (ObjectNull.isNull(appId)) {
                // 首次创建目标版本应用，需要设置默认应用权限，后续迭代不变更应用权限
                jvsApp.setDefaultRole();
                // 生成新的应用id
                appId = IdGenerator.get32UUID();
            }
        } else {
            // 若目标版本是开发版本，且没有默认应用权限，则设置默认应用权限
            if (AppVersionTypeEnum.DEV.equals(targetVersion.getVersionType()) && ObjectNull.isNull(jvsApp.getRole())) {
                jvsApp.setDefaultRole();
            }
        }
        jvsApp.setId(appId);
        if (Boolean.FALSE.equals(AppVersionTypeEnum.DEV.equals(targetVersion.getVersionType()))) {
            jvsApp.setUseVersion(targetVersion.getAppVersion());
        }
        jvsAppService.saveOrUpdate(jvsApp);
    }

    /**
     * 初始化应用信息
     *
     * @param jvsApp 应用信息
     */
    private void initApp(JvsAppTemplate template, JvsApp jvsApp) {
        jvsApp.setName(template.getName());
        jvsApp.setDescription(template.getDescription());
        jvsApp.setLogo(template.getLogo());
        //获取系统名称
//        String shortName = UserCurrentUtils.getCurrentUser().getTenant().getShortName();
//        jvsApp.setPlatform(shortName);
        jvsApp.setFree(template.getFree());
        jvsApp.setPlatform(template.getPlatform());
        jvsApp.setPrice(template.getPrice());
        jvsApp.setAuthorizationKey(authorizationKey());
        jvsApp.setSecret(JvsAppSecretUtils.getAppSecret(IdGenerator.getIdStr()));
        //模板应用的发布状态为也false
        jvsApp.setIsDeploy(false);
        // 通过模板生成的应用，默认启用应用版本功能
        jvsApp.setEnableVersionFeature(Boolean.TRUE);
    }

    /**
     * 获取目标版本的模板信息
     *
     * @param targetCurrentVersion 正在使用的指定版本类型的版本
     * @return 已存在的设计
     */
    private TemplateBo getTargetVersionTemplate(JvsAppVersion targetCurrentVersion) {
        log.info("[getTargetVersionTemplate] 调用 - targetCurrentVersion: {}", targetCurrentVersion != null ? JSONObject.toJSONString(targetCurrentVersion) : "null");
        if (ObjectNull.isNull(targetCurrentVersion) || ObjectNull.isNull(targetCurrentVersion.getTemplateId())) {
            log.info("[getTargetVersionTemplate] targetCurrentVersion或templateId为null，返回空TemplateBo");
            return new TemplateBo();
        }
        JvsAppTemplate template = getAppTemplate(targetCurrentVersion.getTemplateId());
        log.info("[getTargetVersionTemplate] 获取到模板 - template.id={}, template.name={}",
                template != null ? template.getId() : "null",
                template != null ? template.getName() : "null");
        JvsApp jvsApp = JSONObject.parseObject(template.getData(), JvsApp.class);
        log.info("[getTargetVersionTemplate] 解析应用 - jvsApp.id={}, jvsApp.name={}",
                jvsApp != null ? jvsApp.getId() : "null",
                jvsApp != null ? jvsApp.getName() : "null");
        String data = jvsApp.getData();
        // 解密模板内容
        data = des.decryptStr(data);
        TemplateBo result = JSONObject.parseObject(data, TemplateBo.class);
        log.info("[getTargetVersionTemplate] 解析完成 - 菜单目录数量={}, 菜单数量={}",
                result.getAppMenuTypes() != null ? result.getAppMenuTypes().size() : 0,
                result.getAppMenus() != null ? result.getAppMenus().size() : 0);
        if (result.getAppMenus() != null && !result.getAppMenus().isEmpty()) {
            log.info("[getTargetVersionTemplate] 目标版本菜单信息（前3个）:");
            result.getAppMenus().stream().limit(3).forEach(menu -> 
                log.info("  - id={}, name={}, jvsAppId={}", menu.getId(), menu.getName(), menu.getJvsAppId())
            );
        }
        return result;
    }

    /**
     * 获取指定设计id的映射信息
     *
     * @param affiliationAppId  所属应用唯一标识
     * @param designId          设计id
     * @param sourceVersionType 来源版本类型
     * @param mappings          获取应用版本设计id映射集合
     * @return 指定设计id的映射
     */
    private JvsAppVersionMapping getIdMappingDetail(String affiliationAppId, String designId, AppVersionTypeEnum sourceVersionType, List<JvsAppVersionMapping> mappings) {
        return mappings.stream()
                .filter(m -> {
                    if (AppVersionTypeEnum.DEV.equals(sourceVersionType)) {
                        return designId.equals(m.getSourceDesignId());
                    }
                    if (AppVersionTypeEnum.BETA.equals(sourceVersionType)) {
                        // 若是上传离线版本包，需要校验来源id
                        if (VersionIterationTypeEnum.UPLOAD_VERSION.equals(JvsAppTemplateUtils.getContextVersionIterationType())) {
                            // 离线版本包若与当前物理环境是同环境，则离线版本包中的设计id保存在映射表中的betaDesignId中
                            // 离线版本包若与当前物理环境是不是同环境，则离线版本包中的设计id保存在映射表中的sourceDesignId中
                            // 所以上传离线版本包找映射关系，找到其中一个即可
                            return designId.equals(m.getSourceDesignId()) || designId.equals(m.getBetaDesignId());
                        }
                        return designId.equals(m.getBetaDesignId());
                    }
                    if (AppVersionTypeEnum.GA.equals(sourceVersionType)) {
                        return designId.equals(m.getGaDesignId());
                    }
                    return Boolean.FALSE;
                })
                .findAny()
                .orElseGet(() -> new JvsAppVersionMapping().setAffiliationApp(affiliationAppId));
    }

    /**
     * 得到id映射目标版本设计id
     *
     * @param targetVersionType 目标版本类型
     * @param versionMapping    id的映射信息
     * @return 映射的目标版本设计id
     */
    private String getMappingId(AppVersionTypeEnum targetVersionType, JvsAppVersionMapping versionMapping) {
        if (AppVersionTypeEnum.DEV.equals(targetVersionType)) {
            return versionMapping.getSourceDesignId();
        }
        if (AppVersionTypeEnum.BETA.equals(targetVersionType)) {
            return versionMapping.getBetaDesignId();
        }
        if (AppVersionTypeEnum.GA.equals(targetVersionType)) {
            return versionMapping.getGaDesignId();
        }
        return null;
    }

    /**
     * 设置id映射
     *
     * @param newDesignId       新的设计id
     * @param targetVersionType 目标版本类型
     * @param versionMapping    id映射
     */
    private void setIdMapping(String newDesignId, AppVersionTypeEnum targetVersionType, JvsAppVersionMapping versionMapping) {
        if (AppVersionTypeEnum.DEV.equals(targetVersionType)) {
            versionMapping.setSourceDesignId(newDesignId);
        }
        if (AppVersionTypeEnum.BETA.equals(targetVersionType)) {
            versionMapping.setBetaDesignId(newDesignId);
        }
        if (AppVersionTypeEnum.GA.equals(targetVersionType)) {
            versionMapping.setGaDesignId(newDesignId);
        }
    }

    /**
     * 保存版本
     *
     * @param appId                应用id
     * @param sourceVersion        来源版本
     * @param targetCurrentVersion 目标版本
     * @param jvsAppTemplate       来源版本的模板
     * @param templateBo           新版本的设计信息
     */
    public void saveVersionTemplate(String appId, JvsAppVersion sourceVersion, JvsAppVersion targetCurrentVersion, JvsAppTemplate jvsAppTemplate, TemplateBo templateBo) {
        // 目标版本模板id
        String templateId = targetCurrentVersion.getTemplateId();
        // 目标版本没有模板，不管版本类型是否相同，都生成新的模板
        // 目标版本有模板，则版本类型不同时，要生成新模板
        // 离线版本上传的，每次都生成新模板
        // true-为目标版本生成新模板；false-不生成新模板
        boolean createTargetTemplate = ObjectNull.isNull(templateId)
                || Boolean.FALSE.equals(sourceVersion.getVersionType().equals(targetCurrentVersion.getVersionType()))
                || VersionIterationTypeEnum.UPLOAD_VERSION.equals(JvsAppTemplateUtils.getContextVersionIterationType());
        if (createTargetTemplate) {
            // 若目标版本有模板，则查询该模板，否则使用来源版本模板生成新的模板
            JvsAppTemplate targetTemplate = Optional.ofNullable(templateId).map(this::getById).orElseGet(JvsAppTemplate::new);
            jvsAppTemplate.setVersionTemplate(Boolean.TRUE)
                    .setBucketName(targetTemplate.getBucketName())
                    .setFileName(targetTemplate.getFileName())
                    .setId(targetTemplate.getId());
            // 保存新的版本模板
            String ciphertext = encryptData(beanToJsonString(templateBo));
            saveTemplate(templateId, jvsAppTemplate, ciphertext);
            templateId = jvsAppTemplate.getId();
        }
        // 保存版本
        saveAppVersion(appId, sourceVersion, targetCurrentVersion, templateId);
    }

    /**
     * 保存id映射
     *
     * @param newVersionMappings id映射
     * @param templateBo         新的模板设计
     */
    private void saveVersionMapping(List<JvsAppVersionMapping> newVersionMappings, TemplateBo templateBo) {
        if (ObjectNull.isNull(newVersionMappings)) {
            return;
        }
        List<String> dynamicDataIds = Optional.ofNullable(templateBo.getDynamicDataPos()).orElseGet(ArrayList::new).stream().map(DynamicDataPo::getId).collect(Collectors.toList());

        // 将数据分为已存在的和新的两组：key为true表示待新增数据，key为false表示待修改数据
        Map<Boolean, List<JvsAppVersionMapping>> mappingMap = newVersionMappings.stream()
                .filter(mapping -> {
                    if (ObjectNull.isNull(dynamicDataIds)) {
                        return Boolean.TRUE;
                    }
                    // 模型数据id不保存映射关系
                    return Boolean.FALSE.equals(dynamicDataIds.contains(mapping.getSourceDesignId()));
                }).collect(Collectors.groupingBy(mapping -> ObjectNull.isNull(mapping.getId())));
        for (Map.Entry<Boolean, List<JvsAppVersionMapping>> entry : mappingMap.entrySet()) {
            List<JvsAppVersionMapping> mappings = entry.getValue();
            if (ObjectNull.isNull(mappings)) {
                continue;
            }
            if (entry.getKey()) {
                appVersionMappingService.saveBatch(mappings);
            } else {
                appVersionMappingService.updateBatchById(mappings);
            }
        }
    }

    /**
     * 加密
     *
     * @param data 模板明文
     * @return 模板密文
     */
    private String encryptData(String data) {
        return des.encryptBase64(data);
    }

    /**
     * 保存模板
     * 保存到文件服务器
     *
     * @param templateId     模板id为空则新增，否则修改
     * @param jvsAppTemplate 模板信息
     * @param designData     数据密文
     */
    private void saveTemplate(String templateId, JvsAppTemplate jvsAppTemplate, String designData) {
        jvsAppTemplate.setData(designData);
        jvsAppTemplate.setCreateTime(LocalDateTime.now());
        jvsAppTemplate.setId(templateId);
        String body = beanToJsonString(jvsAppTemplate);
        jvsAppTemplate.setData(null);
        // 先删除旧的模板文件
        removeTemplateData(jvsAppTemplate);
        // 模板文件上传到文件服务器
        BaseFile fileNameDto = uploadTemplate(body.getBytes());
        jvsAppTemplate.setSize(fileNameDto.getSize().intValue());
        jvsAppTemplate.setBucketName(fileNameDto.getBucketName());
        jvsAppTemplate.setFileName(fileNameDto.getFileName());
        if (ObjectNull.isNull(templateId)) {
            save(jvsAppTemplate);
        } else {
            updateById(jvsAppTemplate);
        }
    }

    private BaseFile uploadTemplate(byte[] serialize) {
        File dest = new File(IdGenerator.getIdStr() + ".jvs");
        FileUtil.writeBytes(serialize, dest);
        BaseFile put = ossTemplate.put(OSS_BUCKET_NAME, OSS_BUCKET_NAME_PATH, dest, IdGenerator.getIdStr() + ".jvs", true);
        dest.delete();
        return put;
    }

    /**
     * 模板生成应用校验
     *
     * @param templateBo 模板
     */
    private void checkCreateApp(TemplateBo templateBo) {
        // 若有自定义标识，需要校验标识在当前租户下是否已存在，若已存在，则清空模板中的标识
        List<String> identifiers = templateBo.getIdentifiers();
        if (ObjectNull.isNull(identifiers)) {
            return;
        }
        Set<String> existsIdentifiers = identificationService.findByIdentifiers(identifiers).stream().map(Identification::getIdentifier).collect(Collectors.toSet());
        if (ObjectNull.isNotNull(existsIdentifiers)) {
            templateBo.setIdentifications(null);
        }
    }

    /**
     * 校验是否可迭代应用
     *
     * @param affiliationAppId 所属应用唯一标识
     * @param identifiers      自定义标识集合
     */
    private void checkCanIterationApp(String affiliationAppId, List<String> identifiers) {
        if (ObjectNull.isNull(identifiers)) {
            return;
        }
        // Map<标识, 应用id集合>
        Map<String, Set<String>> identifierAppMap = identificationService.findByIdentifiers(identifiers)
                .stream()
                .collect(Collectors.groupingBy(Identification::getIdentifier, Collectors.mapping(Identification::getJvsAppId, Collectors.toSet())));
        if (ObjectNull.isNull(identifierAppMap)) {
            return;
        }
        // 得到迭代应用的id集合
        Set<String> appIds = appVersionService.getVersionTypeAppIdByAffiliationId(affiliationAppId);
        // 可能没数据（如第一次通过离线版本导入，还未入库，所以查不到数据），所以将所属应用唯一标识加入集合
        appIds.add(affiliationAppId);

        // 筛选出已存在的标识，且不是迭代应用使用的标识
        List<String> existsIdentifiers = identifierAppMap.entrySet()
                .stream()
                // 过滤出标识所属应用id与当前迭代应用id没有交集的标识
                .filter(e -> Boolean.FALSE.equals(CollectionUtil.containsAny(e.getValue(), appIds)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (ObjectNull.isNotNull(existsIdentifiers)) {
            throw new BusinessException("当前租户存在与该模板中的自定义标识相同的标识符：", JSON.toJSONString(existsIdentifiers));
        }
    }

    /**
     * 向上兼容(兼容2.1.7的模板，转为2.1.8支持的数据结构)
     *
     * @param appId    应用id
     * @param template 应用模板
     * @param data     模板数据
     * @return 新的模板数据
     */
    private String upwardCompatibilityMenu(String appId, JvsAppTemplate template, String data) {
        JSONObject dataJson = JSON.parseObject(data);
        // 模板中目录不为空，表示生成模板的版本高于2.1.8，不需要向上兼容
        if (ObjectNull.isNotNull(dataJson.getJSONArray(Get.name(TemplateBo::getAppMenuTypes)))) {
            return data;
        }
        // 目录
        List<AppMenuType> appMenuTypes = new ArrayList<>();
        int typeSize = Optional.ofNullable(template.getTypes()).map(List::size).orElseGet(() -> 0);
        for (int i = 0; i < typeSize; i++) {
            if (ObjectNull.isNull(template.getTypes())) {
                continue;
            }
            AppMenuType appMenuType = new AppMenuType()
                    .setId(IdGenerator.getIdStr())
                    .setType(template.getTypes().get(i))
                    .setJvsAppId(appId)
                    .setSort(i);
            List<String> icons = Optional.ofNullable(template.getIcon()).orElseGet(ArrayList::new);
            if (template.getTypes().size() == icons.size()) {
                appMenuType.setIcon(icons.get(i));
            }
            appMenuTypes.add(appMenuType);
        }
        Map<String, String> appMenuTypeMap = appMenuTypes.stream().collect(Collectors.toMap(AppMenuType::getType, AppMenuType::getId));

        // 菜单权限
        List<AppMenu> appMenus = new ArrayList<>();
        // 列表
        List<JSONObject> crudPageList = dataJson.getList(Get.name(TemplateBo::getCrudPageList), JSONObject.class);
        if (ObjectNull.isNotNull(crudPageList)) {
            crudPageList.forEach(page -> {
                AppMenu appMenu = BeanCopyUtil.copy(page, AppMenu.class);
                appMenu.setId(null)
                        .setDesignType(DesignType.page)
                        .setDesignId(page.getString(Get.name(CrudPage::getId)));
                if (ObjectNull.isNotNull(page.get(Get.name(CrudPage::getType)))) {
                    appMenu.setType(appMenuTypeMap.get(appMenu.getType()));
                }
                String viewJson = page.getString(Get.name(CrudPage::getViewJson));
                if (ObjectNull.isNotNull(viewJson)) {
                    appMenu.setPermission(DesignPermissionUtil.parseDesign(DesignType.page, viewJson));
                }
                appMenus.add(appMenu);
            });
        }
        // 表单
        List<JSONObject> formList = dataJson.getList(Get.name(TemplateBo::getFormPoList), JSONObject.class);
        if (ObjectNull.isNotNull(formList)) {
            formList.forEach(form -> {
                AppMenu appMenu = BeanCopyUtil.copy(form, AppMenu.class);
                appMenu.setId(null)
                        .setDesignType(DesignType.form)
                        .setDesignId(form.getString(Get.name(FormPo::getId)));
                if (ObjectNull.isNotNull(form.get(Get.name(FormPo::getType)))) {
                    appMenu.setType(appMenuTypeMap.get(appMenu.getType()));
                }
                String viewJson = form.getString(Get.name(FormPo::getViewJson));
                if (ObjectNull.isNotNull(viewJson)) {
                    appMenu.setPermission(DesignPermissionUtil.parseDesign(DesignType.form, viewJson));
                }
                appMenus.add(appMenu);
            });
        }

        // 自定义页面
        List<JSONObject> appUrlList = dataJson.getList(Get.name(TemplateBo::getAppUrlPos), JSONObject.class);
        if (ObjectNull.isNotNull(appUrlList)) {
            appUrlList.forEach(url -> {
                AppMenu appMenu = BeanCopyUtil.copy(url, AppMenu.class);
                appMenu.setId(null).setType(appMenuTypeMap.get(appMenu.getType())).setDesignType(DesignType.URL).setDesignId(url.getString(Get.name(AppUrlPo::getId)));
                appMenus.add(appMenu);
            });
        }

        dataJson.put(Get.name(TemplateBo::getAppMenuTypes), appMenuTypes);
        dataJson.put(Get.name(TemplateBo::getAppMenus), appMenus);
        return dataJson.toJSONString();
    }

    /**
     * 创建应用成功，发送消息
     *
     * @param type   TRUE-模板创建应用， FALSE-版本迭代
     * @param jvsApp 新创建的应用
     */
    private void sendCreateAppNotice(boolean type, JvsApp jvsApp) {
        try {
            //发送站内信
            InsideNotificationDto interiorMessage = new InsideNotificationDto();
            //拼装数据
            String str = type ? "轻应用创建成功" : "版本迭代成功";
            Dict set = Dict.create().set("title", jvsApp.getName() + str).set("content", jvsApp.getLongText());
            interiorMessage.setContent(JSONObject.toJSONString(set));
            List<ReceiversDto> receiversDtoList = new ArrayList<>();
            receiversDtoList.add(new ReceiversDto().setUserName(UserCurrentUtils.getRealName()).setUserId(UserCurrentUtils.getUserId()));
            interiorMessage.setDefinedReceivers(receiversDtoList);
            insideNotificationApi.send(interiorMessage);
        } catch (Exception e) {
            log.error("发送创建应用成功消息失败：" + e.getMessage());
        }
    }

    /**
     * 根据key生成是否是收费和免费的凭证，每一台服务器都不一样,确定是否付费解除授权凭证
     *
     * @return
     */
    private String authorizationKey() {
        return IdGenerator.getIdStr(34);
    }

    @Override
    public String getDesignData(JvsAppTemplate jvsAppTemplate) {
        String jvsAppId = jvsAppTemplate.getId();
        // 置空，避免OOM
        jvsAppTemplate.setData(null);
        JvsApp jvsApp = BeanCopyUtil.copy(jvsAppTemplate, JvsApp.class);
        List<String> ids = new ArrayList<>(20);
        //将应用ID也添加进去
        ids.add(jvsAppId);
        // 目录
        List<AppMenuType> appMenuTypes = appMenuTypeTemplateService.list(jvsAppId, ids);
        //查询模型
        List<DataModelPo> dataModelList = dataModelTemplateService.list(jvsAppId, ids);
        //得到模型id集合
        List<String> modelIds = dataModelList.stream().map(DataModelPo::getId).collect(Collectors.toList());

        //列表页
        List<CrudPage> pageList = crudPageTemplateService.list(jvsAppId, ids);
        //表单
        List<FormPo> formList = formTemplateService.list(jvsAppId, ids);

        //字段
        List<DataFieldPo> dataFieldPoList = dataFieldTemplateService.list(modelIds, ids);
        //数据
        Boolean deployData = Optional.ofNullable(jvsAppTemplate.getDeployData()).orElse(Boolean.FALSE);
        // 查询指定模型的数据
        List<DynamicDataPo> dynamicDataPoList = deployData ? dynamicDataTemplateService.list(jvsAppTemplate.getDeployDataModelIds(), ids) : null;

        // 模型数据自增id
        List<DataIdPo> dataIdPoList = deployData ? dataIdTemplateService.list(modelIds, ids) : null;

        // 图表改为API调用，暂不支持发布到模板
//        List<Chart> chartPage = getChartPage(jvsAppId, ids);
        //url
        List<AppUrlPo> jvsAppUrl = appUrlTemplateService.list(jvsAppId, ids);
        // 工作流相关
        List<FlowDesign> flowDesignList = flowDesignTemplateService.list(jvsAppId, ids);
        List<FlowDesignVersion> flowDesignVersionList = flowDesignVersionTemplateService.list(jvsAppId, ids);
        List<FlowPurview> flowPurviewList = flowPurviewTemplateService.list(jvsAppId, ids);
        // 业务关联规则
        List<CrudAssociationPo> crudAssociationPoList = associationTemplateService.list(jvsAppId, ids);
        List<RuleDesignPo> ruleDesignList = ruleDesignTemplateService.list(jvsAppId, ids);
        List<FunctionBusinessPo> functionBusiness = functionBusinessTemplateService.list(jvsAppId, ids);
        // DataEventService
        List<DataEventPo> dataEventPoList = dataEventTemplateService.list(jvsAppId, ids);
        // 消息通知
        List<DataNoticePo> dataNoticePoList = dataNoticeTemplateService.list(jvsAppId, ids);
        // 打印模板
        List<PrintTemplate> printTemplateList = printTemplateTemplateService.list(jvsAppId, ids);
        // 外部页面接入
        List<ExternalPage> externalPageList = externalPageTemplateService.list(jvsAppId, ids);
        // 轻应用菜单权限
        List<AppMenu> appMenus = appMenuTemplateService.list(jvsApp, ids);
        // 标识
        List<Identification> identifications = identificationTemplateService.list(jvsAppId, ids);

        TemplateBo templateBo = new TemplateBo();
        templateBo.setAppUrlPos(jvsAppUrl);
        templateBo.setIds(ids);
        templateBo.setFormPoList(formList);
        templateBo.setCrudPageList(pageList);
        templateBo.setDataFieldPos(dataFieldPoList);
        templateBo.setDynamicDataPos(dynamicDataPoList);
        templateBo.setDataModelPoList(dataModelList);
        templateBo.setFlowDesigns(flowDesignList);
        templateBo.setFlowDesignVersions(flowDesignVersionList);
        templateBo.setFlowPurviews(flowPurviewList);
        templateBo.setRuleDesignPos(ruleDesignList);
        templateBo.setFunctionBusinessPos(functionBusiness);
        templateBo.setCrudAssociationPos(crudAssociationPoList);
        templateBo.setDataEventPos(dataEventPoList);
        templateBo.setDataNoticePos(dataNoticePoList);
        templateBo.setPrintTemplates(printTemplateList);
        templateBo.setExternalPages(externalPageList);
        templateBo.setAppMenuTypes(appMenuTypes);
        templateBo.setAppMenus(appMenus);
        templateBo.setIdentifications(identifications);
        templateBo.setIdentifiers(identifications.stream().map(Identification::getIdentifier).collect(Collectors.toList()));
        templateBo.setDataIdPoList(dataIdPoList);

        String s = beanToJsonString(templateBo);
        return encryptData(s);
    }

    private <T> String beanToJsonString(T bean) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule simpleModule = new SimpleModule();
            //  LocalDateTime时间格式化
            simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            objectMapper.registerModule(simpleModule);
            return objectMapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            log.error("bean转json字符串失败", e);
            throw new BusinessException("生成模板数据失败", e);
        }
    }

    private <T> T stringToBean(String data, Class<T> cls) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule simpleModule = new SimpleModule();
            //  LocalDateTime时间格式化
            simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            objectMapper.registerModule(simpleModule);
            return objectMapper.readValue(data, cls);
        } catch (JsonProcessingException e) {
            log.error("json字符串转bean失败", e);
            throw new BusinessException("生成模板数据失败", e);
        }
    }

    @Override
    public String getData(JvsAppTemplate template) {
        // 没有文件名，从数据库查询
        if (ObjectNull.isNull(template.getFileName())) {
            return templateDataMapper.selectList(Wrappers.<JvsAppTemplateData>lambdaQuery()
                            .eq(JvsAppTemplateData::getTemplateId, template.getId())
                            .orderByAsc(JvsAppTemplateData::getSort))
                    .stream()
                    .filter(ObjectNull::isNotNull)
                    .map(JvsAppTemplateData::getData)
                    .collect(Collectors.joining());
        }
        // 从文件中获取数据
        InputStream inputStream = ossTemplate.getObject(template.getBucketName(), template.getFileName());
        byte[] bytes = IoUtil.readBytes(inputStream);
        return new String(bytes);
    }

    @Override
    public void saveTemplate(JvsAppTemplate jvsAppTemplate) {
        //只支持在线服务
        //获取这个应用下所有的数据
        String designData = getDesignData(jvsAppTemplate);
        saveTemplate(null, jvsAppTemplate, designData);
    }

    @Async
    @Override
    public void saveTemplateAsync(JvsAppTemplate jvsAppTemplate, String userId, String realName, String tenantId) {
        //只支持在线服务
        //获取这个应用下所有的数据
        String designData = getDesignData(jvsAppTemplate);
        saveTemplate(null, jvsAppTemplate, designData);
        //发送消息
        //拼装数据
        Dict set = Dict.create().set("title", jvsAppTemplate.getName() + "模板发布成功").set("content", jvsAppTemplate.getName() + "模板发布成功");
        InsideNotificationDto interiorMessage = new InsideNotificationDto();
        interiorMessage.setContent(JSONObject.toJSONString(set));
        List<ReceiversDto> receiversDtos = new ArrayList<>();
        receiversDtos.add(new ReceiversDto().setUserId(userId).setUserName(realName).setTenantId(tenantId));
        interiorMessage.setDefinedReceivers(receiversDtos);
        interiorMessage.setTenantId(TenantContextHolder.getTenantId());
        insideNotificationApi.send(interiorMessage);
    }

    @Override
    public void saveTemplate(String templateId, JvsAppTemplate jvsAppTemplate) {
        //只支持在线服务
        //获取这个应用下所有的数据
        String designData = getDesignData(jvsAppTemplate);
        saveTemplate(templateId, jvsAppTemplate, designData);
    }

    @Override
    public void saveUploadTemplate(JvsAppTemplate jvsAppTemplate) {
        String body = jvsAppTemplate.getData();
        jvsAppTemplate.setDeploy(false);
        jvsAppTemplate.setData(null);
        BaseFile fileNameDto = uploadTemplate(body.getBytes());
        jvsAppTemplate.setSize(fileNameDto.getSize().intValue());
        jvsAppTemplate.setBucketName(fileNameDto.getBucketName());
        jvsAppTemplate.setFileName(fileNameDto.getFileName());
        jvsAppTemplate.setCreateById(UserCurrentUtils.getUserId());
        jvsAppTemplate.setCreateBy(UserCurrentUtils.getRealName());
        save(jvsAppTemplate);
    }

    @Override
    public JvsAppVersion initVersionDevTemplate(JvsApp jvsApp) {
        JvsAppTemplate jvsAppTemplate = new JvsAppTemplate()
                .setName(jvsApp.getName())
                .setId(jvsApp.getId())
                .setVersionTemplate(Boolean.TRUE);
        saveTemplate(jvsAppTemplate);
        JvsAppVersion version = new JvsAppVersion()
                .setJvsAppId(jvsApp.getId())
                .setAffiliationApp(jvsApp.getId())
                .setDescription("")
                .setVersionType(AppVersionTypeEnum.DEV)
                .setAppVersion(AppConstant.DEFAULT_INIT_APP_VERSION)
                .setVersionStatus(AppVersionStatusEnum.USE)
                .setTemplateId(jvsAppTemplate.getId());
        appVersionService.saveVersion(version);
        return version;
    }


    @Override
    public void saveAppVersion(String appId, JvsAppVersion sourceVersion, JvsAppVersion targetVersion, String templateId) {
        // 保存版本
        String affiliationApp = Optional.ofNullable(sourceVersion.getAffiliationApp()).orElse(appId);
        targetVersion
                .setJvsAppId(appId)
                .setAffiliationApp(affiliationApp)
                .setDescription(sourceVersion.getDescription())
                .setAppVersion(sourceVersion.getAppVersion())
                .setVersionStatus(AppVersionStatusEnum.USE)
                .setTemplateId(templateId);
        appVersionService.saveVersion(targetVersion);
        // 若目标版本是正式版本，则将该应用改为已发布
        if (AppVersionTypeEnum.GA.equals(targetVersion.getVersionType())) {
            jvsAppService.deploy(targetVersion.getJvsAppId());
        }
    }

    @Override
    public void removeTemplateData(JvsAppTemplate template) {
        if (ObjectNull.isNotNull(template.getFileName())) {
            ossTemplate.removeFile(template.getBucketName(), template.getFileName());
            return;
        }
        if (ObjectNull.isNotNull(template.getId())) {
            templateDataMapper.delete(Wrappers.query(new JvsAppTemplateData().setTemplateId(template.getId())));
        }
    }
}
