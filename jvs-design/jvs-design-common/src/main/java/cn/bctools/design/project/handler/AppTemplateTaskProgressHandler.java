package cn.bctools.design.project.handler;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgressDetail;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressDetailEnum;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressDetailService;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressService;
import cn.bctools.design.project.utils.AppTemplateTaskUtils;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 应用模板创建或迭代应用任务进度
 */
@Slf4j
@Component
@AllArgsConstructor
public class AppTemplateTaskProgressHandler {

    private final RedisUtils redisUtils;
    private final JvsAppTemplateTaskProgressService progressService;
    private final JvsAppTemplateTaskProgressDetailService progressDetailService;
    /**
     * 截取异常栈长度
     */
    private static final int MAX_STACK_TRACE_LENGTH = 1000;

    /**
     * 迭代任务心跳集合
     */
    private static final Map<String, String> APP_ITERATOR_TASK_HEARTBEAT = new HashMap<>();

    /**
     * 心跳超时时间. 超过该时长，表示该任务已经挂了
     */
    private static final Long TASK_HEARTBEAT_MAX_TIME_OUT = 2 * 60 * 1000L;

    /**
     * 服务停止，未结束的任务异常提示
     */
    private static final String SERVER_DESTROY_TASK_ERROR_MESSAGE = "服务停止或重启导致安装失败";

    @PostConstruct
    private void heart() {
        // 每10秒发送一次心跳
        CronUtil.schedule("*/10 * * * * *", (Task) () -> {
            try {
                sendHeartbeat();
            } catch (Exception e) {
                log.error("每10秒发送一次任务心跳：{}", e.getMessage());
            }
        });

        // 每分钟检查一次心跳
        CronUtil.schedule("0 * * * * *", (Task) () -> {
            try {
                checkHeartbeat();
            } catch (Exception e) {
                log.error("每分钟检查一次任务心跳：{}", e.getMessage());
            }
        });
    }

    @PreDestroy
    private void destroy() {
        APP_ITERATOR_TASK_HEARTBEAT.forEach((key, value) -> {
            removeHeart(key, SERVER_DESTROY_TASK_ERROR_MESSAGE);
            redisUtils.unLock(key);
        });
    }


    /**
     * 发送心跳
     */
    private void sendHeartbeat() {
        APP_ITERATOR_TASK_HEARTBEAT.forEach(this::sendHeartbeat);
    }

    /**
     * 删除心跳
     *
     * @param lockey   锁
     * @param errorMsg 异常消息
     */
    public void removeHeart(String lockey, String errorMsg) {
        String taskId = APP_ITERATOR_TASK_HEARTBEAT.get(lockey);
        if (ObjectNull.isNotNull(errorMsg)) {
            taskFailureEnd(taskId, "失败");
        }
        // 删除心跳缓存
        AppTemplateTaskUtils.removeHeart(getHeartbeatItemKey(lockey, taskId));
    }

    /**
     * 删除心跳
     *
     * @param lockey   锁
     * @param errorMsg 异常消息
     */
    public void removeHeartAndCache(String lockey, String errorMsg) {
        removeHeart(lockey, errorMsg);
        APP_ITERATOR_TASK_HEARTBEAT.remove(lockey);
    }

    /**
     * 发送心跳
     *
     * @param lockey 任务锁
     * @param taskId 任务id
     */
    private void sendHeartbeat(String lockey, String taskId) {
        AppTemplateTaskUtils.updateHeart(getHeartbeatItemKey(lockey, taskId), System.currentTimeMillis());
    }

    /**
     * 获取任务心跳缓存项
     *
     * @param lockey 任务锁
     * @param taskId 任务id
     * @return 心跳缓存项
     */
    private String getHeartbeatItemKey(String lockey, String taskId) {
        return lockey + "_" + taskId;
    }

    /**
     * 检查任务心跳
     */
    private void checkHeartbeat() {
        log.debug("本地缓存心跳任务数量：{}", APP_ITERATOR_TASK_HEARTBEAT.size());
        // 所有微服务，同时只需要有一个检查任务心跳即可
        String checkLockKey = AppTemplateTaskUtils.getCheckLockKey();
        boolean lock = AppTemplateTaskUtils.tryLockCheckHeart(checkLockKey);
        if (Boolean.FALSE.equals(lock)) {
            return;
        }
        try {
            Map<Object, Object> heartMap = AppTemplateTaskUtils.getAllHeart();
            if (ObjectNull.isNull(heartMap)) {
                return;
            }
            long now = System.currentTimeMillis();
            List<String> heartbeatTimeOutKey = heartMap.entrySet().stream()
                    .filter(e -> now - (Long) e.getValue() > TASK_HEARTBEAT_MAX_TIME_OUT)
                    .map(e -> (String) e.getKey())
                    .collect(Collectors.toList());
            if (ObjectNull.isNull(heartbeatTimeOutKey)) {
                return;
            }

            heartbeatTimeOutKey.forEach(h -> {
                String[] arr = h.split("_");
                String lockey = arr[0];
                String taskId = arr[1];
                AppTemplateTaskUtils.unLock(lockey);
                taskFailureEnd(taskId, SERVER_DESTROY_TASK_ERROR_MESSAGE);
            });

            AppTemplateTaskUtils.removeHeart(heartbeatTimeOutKey.toArray());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            AppTemplateTaskUtils.unLock(checkLockKey);
        }
    }

    /**
     * 任务异常停止
     *
     * @param taskId       任务id
     * @param errorMessage 异常消息
     */
    private void taskFailureEnd(String taskId, String errorMessage) {
        String appId = AppTemplateTaskUtils.getTaskAppId(taskId);
        JvsAppTemplateTaskProgress progress = AppTemplateTaskUtils.getTaskProgress(appId, taskId);
        if (ObjectNull.isNull(progress)) {
            return;
        }
        TenantContextHolder.setTenantId(progress.getTenantId());
        addProgress(taskId, AppTemplateTaskProgressDetailEnum.END, AppTemplateTaskProgressEnum.FAILURE, null, errorMessage);
        progressService.end(taskId, AppTemplateTaskProgressEnum.FAILURE);
        TenantContextHolder.clear();
    }

    /**
     * 初始化任务
     *
     * @param summary        任务概要
     * @param affiliationApp 所属应用唯一标识
     * @return
     */
    public JvsAppTemplateTaskProgress initTask(String lockKey, @NotNull String summary, String affiliationApp) {
        // 初始化任务
        JvsAppTemplateTaskProgress templateTask = new JvsAppTemplateTaskProgress()
                .setId(IdGenerator.getIdStr())
                .setSummary(summary)
                .setJvsAppId(affiliationApp)
                .setProgress(AppTemplateTaskProgressEnum.PROCESSING)
                .setTenantId(TenantContextHolder.getTenantId());
        templateTask.setCreateTime(LocalDateTime.now());
        templateTask.setUpdateTime(LocalDateTime.now());
        templateTask.setCreateById(UserCurrentUtils.getUserId());

        String taskId = templateTask.getId();
        AppTemplateTaskUtils.cacheProgress(affiliationApp, taskId, JSON.toJSONString(templateTask));
        AppTemplateTaskUtils.cacheTaskAppId(affiliationApp, taskId);
        AppTemplateTaskUtils.cacheUserTaskId(UserCurrentUtils.getUserId(), taskId);

        // 将任务锁key加入待发送心跳集合
        APP_ITERATOR_TASK_HEARTBEAT.put(lockKey, templateTask.getId());
        log.debug("初始化心跳key：" + "锁：" + lockKey + " 任务id：" + templateTask.getId());
        sendHeartbeat(lockKey, templateTask.getId());
        log.debug("添加本地缓存心跳任务：{}, 总数：{}", lockKey, APP_ITERATOR_TASK_HEARTBEAT.size());
        return templateTask;
    }

    /**
     * 修改进度应用id
     *
     * @param taskId 任务进度id
     * @param appId  应用id
     */
    public void updateAppId(String taskId, String appId, String oldAppId) {
        JvsAppTemplateTaskProgress progressTask = AppTemplateTaskUtils.getTaskProgress(oldAppId, taskId);
        if (ObjectNull.isNull(progressTask)) {
            return;
        }
        progressTask.setJvsAppId(appId);
        AppTemplateTaskUtils.removeProgressCache(appId, taskId);
        AppTemplateTaskUtils.cacheProgress(appId, taskId, JSON.toJSONString(progressTask));
        AppTemplateTaskUtils.cacheTaskAppId(appId, taskId);
        AppTemplateTaskUtils.cacheUserTaskId(UserCurrentUtils.getUserId(), taskId);

    }

    /**
     * 添加进度
     *
     * @param taskId           任务进度id
     * @param taskProgressStep 步骤
     * @param progress         进度
     */
    public void addProgress(String taskId, AppTemplateTaskProgressDetailEnum taskProgressStep, AppTemplateTaskProgressEnum progress) {
        addProgress(taskId, taskProgressStep, progress, null, null);
    }

    /**
     * 添加进度
     *
     * @param taskId           任务进度id
     * @param taskProgressStep 步骤
     * @param progress         进度
     * @param duration         耗时
     * @param content          内容
     */
    public void addProgress(String taskId, AppTemplateTaskProgressDetailEnum taskProgressStep, AppTemplateTaskProgressEnum progress, Long duration, String content) {
        String contentStr = content == null ? taskProgressStep.getDefaultContent() : content;
        JvsAppTemplateTaskProgressDetail templateTaskLog = new JvsAppTemplateTaskProgressDetail()
                .setTaskId(taskId)
                .setCode(taskProgressStep.name())
                .setContent(contentStr)
                .setProgress(progress)
                .setSerialNumber(taskProgressStep.getSerialNumber())
                .setDuration(duration);
        templateTaskLog.setCreateTime(LocalDateTime.now());

        AppTemplateTaskUtils.cacheProgressDetail(taskId, taskProgressStep.name(), JSON.toJSONString(templateTaskLog));
    }


    /**
     * 初始化创建或迭代应用进度步骤
     *
     * @param taskId 任务进度id
     */
    public void initCreateOrIterationProgress(String taskId) {
        Map<String, Object> inits = AppTemplateTaskProgressDetailEnum.getCreateOrIterationInitEnum().stream()
                .map(e ->
                        new JvsAppTemplateTaskProgressDetail()
                                .setTaskId(taskId)
                                .setCode(e.name())
                                .setContent(e.getDefaultContent())
                                .setProgress(AppTemplateTaskProgressEnum.WAIT)
                                .setSerialNumber(e.getSerialNumber())
                                .setCreateTime(LocalDateTime.now()))
                .collect(Collectors.toMap(JvsAppTemplateTaskProgressDetail::getCode, JSON::toJSONString));

        AppTemplateTaskUtils.cacheProgressDetails(taskId, inits);
    }

    /**
     * 执行任务并保存进度
     *
     * @param taskProgress     任务进度
     * @param taskProgressStep 任务日志类型
     * @param method           待执行的业务方法
     */
    public void runTask(JvsAppTemplateTaskProgress taskProgress, AppTemplateTaskProgressDetailEnum taskProgressStep, Runnable method) {
        String taskId = taskProgress.getId();
        // 修改任务日志状态为处理中
        changeProgress(taskId, taskProgressStep, AppTemplateTaskProgressEnum.PROCESSING);
        long startTime = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now());
        try {
            // 执行任务
            method.run();
            // 修改任务日志状态为成功
            long duration = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()) - startTime;
            changeProgress(taskId, taskProgressStep, AppTemplateTaskProgressEnum.SUCCESS, duration);
        } catch (Exception e) {
            // 修改任务日志状态为失败
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String printStackTraceStr = sw.toString().substring(0, Math.min(sw.toString().length(), MAX_STACK_TRACE_LENGTH));
            long duration = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()) - startTime;
            changeProgress(taskId, taskProgressStep, AppTemplateTaskProgressEnum.FAILURE, duration, printStackTraceStr);

            // 任务结束
            long totalDuration = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()) - LocalDateTimeUtil.toEpochMilli(taskProgress.getCreateTime());
            addProgress(taskId, AppTemplateTaskProgressDetailEnum.END, AppTemplateTaskProgressEnum.FAILURE, totalDuration, "失败");
            progressService.end(taskId, AppTemplateTaskProgressEnum.FAILURE);
            throw new BusinessException(e.getMessage());
        }
    }

    /**
     * 完成任务
     *
     * @param templateTaskProgress 任务进度
     */
    public void processEnd(JvsAppTemplateTaskProgress templateTaskProgress) {
        // 任务结束
        String taskProgressId = templateTaskProgress.getId();
        long totalDuration = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()) - LocalDateTimeUtil.toEpochMilli(templateTaskProgress.getCreateTime());
        addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.END, AppTemplateTaskProgressEnum.SUCCESS, totalDuration, "完成");
        progressService.end(taskProgressId, AppTemplateTaskProgressEnum.SUCCESS);
    }


    /**
     * 变更日志
     *
     * @param taskId      任务id
     * @param taskLogEnum 任务日志枚举
     * @param progress    进度
     */
    public void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress) {
        changeProgress(taskId, taskLogEnum, progress, null);
    }

    /**
     * 变更日志
     *
     * @param taskId      任务id
     * @param taskLogEnum 任务日志枚举
     * @param progress    任务日志状态枚举
     * @param duration    耗时(ms)
     */
    public void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress, Long duration) {
        changeProgress(taskId, taskLogEnum, progress, duration, null);
    }

    /**
     * 变更日志
     *
     * @param taskId              任务id
     * @param taskLogEnum         任务日志枚举
     * @param progress            任务日志状态枚举
     * @param duration            耗时(ms)
     * @param exceptionStackTrace 异常栈
     */
    public void changeProgress(String taskId, AppTemplateTaskProgressDetailEnum taskLogEnum, AppTemplateTaskProgressEnum progress, Long duration, String exceptionStackTrace) {
        JvsAppTemplateTaskProgressDetail detail = AppTemplateTaskUtils.getDetail(taskId, taskLogEnum.name());
        detail.setProgress(progress);
        if (ObjectNull.isNotNull(duration)) {
            detail.setDuration(duration);
        }
        if (ObjectNull.isNotNull(exceptionStackTrace)) {
            detail.setExceptionStackTrace(exceptionStackTrace);
        }
        AppTemplateTaskUtils.cacheProgressDetail(taskId, taskLogEnum.name(), JSON.toJSONString(detail));
    }



    /**
     * 获取指定应用进行中的迭代任务
     *
     * @param appId 应用id
     * @return 进行中的迭代任务
     */
    public List<JvsAppTemplateTaskProgress> getProcessingProgressByApp(String appId) {
        return AppTemplateTaskUtils.listTaskProgress(appId);

    }


    /**
     * 获取指定应用进行中的迭代任务详情
     *
     * @param taskId 任务id
     * @return 进行中的迭代任务
     */
    public List<JvsAppTemplateTaskProgressDetail> getProcessingProgressDetail(String taskId) {
        Map<Object, Object> detailMap = AppTemplateTaskUtils.listDetail(taskId);
        if (ObjectNull.isNull(detailMap)) {
            return Collections.emptyList();
        }
        return detailMap.values()
                .stream()
                .map(detail -> JSON.parseObject((String) detail, JvsAppTemplateTaskProgressDetail.class))
                .sorted(Comparator.comparing(JvsAppTemplateTaskProgressDetail::getCreateTime).thenComparing(JvsAppTemplateTaskProgressDetail::getSerialNumber))
                .collect(Collectors.toList());

    }

    /**
     * 获取用户最近发起的进行中的迭代任务
     *
     * @param userId 用户id
     * @return 进行中的迭代任务
     */
    public List<JvsAppTemplateTaskProgress> getUserProcessingProgress(String userId) {
        Set<Object> userTaskIds = AppTemplateTaskUtils.listUserTaskIds(userId);
        if (ObjectNull.isNull(userTaskIds)) {
            return Collections.emptyList();
        }
        return userTaskIds.stream()
                .map(tid -> {
                    String taskId = (String) tid;
                    String appId = AppTemplateTaskUtils.getTaskAppId(taskId);
                    return AppTemplateTaskUtils.getTaskProgress(appId, taskId);
                })
                .filter(ObjectNull::isNotNull)
                .sorted(Comparator.comparing(JvsAppTemplateTaskProgress::getCreateTime).reversed())
                .collect(Collectors.toList());

    }
}
