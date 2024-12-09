package cn.bctools.design.project.handler;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgressDetail;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressDetailEnum;
import cn.bctools.design.project.entity.enums.AppTemplateTaskProgressEnum;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressDetailService;
import cn.bctools.design.project.service.JvsAppTemplateTaskProgressService;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 心跳key前缀
     */
    private static final String TASK_HEARTBEAT_KEY = "template:app:progress";
    /**
     * 心跳超时时间. 超过该时长，表示该任务已经挂了
     */
    private static final Long TASK_HEARTBEAT_MAX_TIME_OUT = 2 * 60 * 1000L;
    /**
     * 检查心跳锁
     */
    private static final String CHECK_LOCK = "template:app:progress:lock";
    /**
     * 检查心跳锁时长
     */
    private static final Integer CHECK_LOCK_TTL = 60;
    /**
     * 缓存项过期时长
     */
    private static final Long TTL = 86400L;

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
     * 心跳key
     */
    private String getHeartKey() {
        return SysConstant.redisKey(TASK_HEARTBEAT_KEY, "heart");
    }


    /**
     * 检查心跳锁key
     */
    private String getCheckLockKey() {
        return SysConstant.redisKey(CHECK_LOCK, "check");
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
        redisUtils.hdel(getHeartKey(), getHeartbeatItemKey(lockey, taskId));
        APP_ITERATOR_TASK_HEARTBEAT.remove(lockey);
    }

    /**
     * 发送心跳
     *
     * @param lockey 任务锁
     * @param taskId 任务id
     */
    private void sendHeartbeat(String lockey, String taskId) {
        redisUtils.hset(getHeartKey(), getHeartbeatItemKey(lockey, taskId), System.currentTimeMillis(), TTL);
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
        String checkLockKey = getCheckLockKey();
        boolean lock = redisUtils.tryLock(checkLockKey, CHECK_LOCK_TTL);
        if (Boolean.FALSE.equals(lock)) {
            return;
        }
        try {
            Map<Object, Object> heartMap = redisUtils.hmget(getHeartKey());
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
                redisUtils.unLock(lockey);
                taskFailureEnd(taskId, SERVER_DESTROY_TASK_ERROR_MESSAGE);
            });

            redisUtils.hdel(getHeartKey(), heartbeatTimeOutKey.toArray());
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            redisUtils.unLock(checkLockKey);
        }
    }

    /**
     * 任务异常停止
     *
     * @param taskId       任务id
     * @param errorMessage 异常消息
     */
    private void taskFailureEnd(String taskId, String errorMessage) {
        JvsAppTemplateTaskProgress progress = progressService.getById(taskId);
        TenantContextHolder.setTenantId(progress.getTenantId());
        progressService.addProgress(taskId, AppTemplateTaskProgressDetailEnum.END, AppTemplateTaskProgressEnum.FAILURE, null, errorMessage);
        progressService.updateFailureProgress(taskId);
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public JvsAppTemplateTaskProgress initTask(String lockKey, @NotNull String summary, String affiliationApp) {
        // 初始化任务
        JvsAppTemplateTaskProgress templateTask = new JvsAppTemplateTaskProgress()
                .setSummary(summary)
                .setJvsAppId(affiliationApp)
                .setProgress(AppTemplateTaskProgressEnum.PROCESSING);
        progressService.save(templateTask);

        // 将任务锁key加入待发送心跳集合
        APP_ITERATOR_TASK_HEARTBEAT.put(lockKey, templateTask.getId());
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateAppId(String taskId, String appId) {
        progressService.update(Wrappers.<JvsAppTemplateTaskProgress>lambdaUpdate()
                .set(JvsAppTemplateTaskProgress::getJvsAppId, appId)
                .eq(JvsAppTemplateTaskProgress::getId, taskId));
    }

    /**
     * 添加进度
     *
     * @param taskId           任务进度id
     * @param taskProgressStep 步骤
     * @param progress         进度
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void addProgress(String taskId, AppTemplateTaskProgressDetailEnum taskProgressStep, AppTemplateTaskProgressEnum progress, Long duration, String content) {
        String contentStr = content == null ? taskProgressStep.getDefaultContent() : content;
        JvsAppTemplateTaskProgressDetail templateTaskLog = new JvsAppTemplateTaskProgressDetail()
                .setTaskId(taskId)
                .setCode(taskProgressStep.name())
                .setContent(contentStr)
                .setProgress(progress)
                .setSerialNumber(taskProgressStep.getSerialNumber())
                .setDuration(duration);
        progressDetailService.save(templateTaskLog);
    }

    /**
     * 初始化创建或迭代应用进度步骤
     *
     * @param taskId 任务进度id
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void initCreateOrIterationProgress(String taskId) {
        List<JvsAppTemplateTaskProgressDetail> inits = AppTemplateTaskProgressDetailEnum.getCreateOrIterationInitEnum().stream()
                .map(e ->
                        new JvsAppTemplateTaskProgressDetail()
                                .setTaskId(taskId)
                                .setCode(e.name())
                                .setContent(e.getDefaultContent())
                                .setProgress(AppTemplateTaskProgressEnum.WAIT)
                                .setSerialNumber(e.getSerialNumber()))
                .collect(Collectors.toList());
        progressDetailService.saveBatch(inits);
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
        progressService.changeProgress(taskId, taskProgressStep, AppTemplateTaskProgressEnum.PROCESSING);
        long startTime = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now());
        try {
            // 执行任务
            method.run();
            // 修改任务日志状态为成功
            long duration = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()) - startTime;
            progressService.changeProgress(taskId, taskProgressStep, AppTemplateTaskProgressEnum.SUCCESS, duration);
        } catch (Exception e) {
            // 修改任务日志状态为失败
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String printStackTraceStr = sw.toString().substring(0, Math.min(sw.toString().length(), MAX_STACK_TRACE_LENGTH));
            long duration = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()) - startTime;
            progressService.changeProgress(taskId, taskProgressStep, AppTemplateTaskProgressEnum.FAILURE, duration, printStackTraceStr);

            // 任务结束
            long totalDuration = LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()) - LocalDateTimeUtil.toEpochMilli(taskProgress.getCreateTime());
            progressService.addProgress(taskId, AppTemplateTaskProgressDetailEnum.END, AppTemplateTaskProgressEnum.FAILURE, totalDuration, "失败");
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
        progressService.addProgress(taskProgressId, AppTemplateTaskProgressDetailEnum.END, AppTemplateTaskProgressEnum.SUCCESS, totalDuration, "完成");
        progressService.end(taskProgressId, AppTemplateTaskProgressEnum.SUCCESS);
    }

}
