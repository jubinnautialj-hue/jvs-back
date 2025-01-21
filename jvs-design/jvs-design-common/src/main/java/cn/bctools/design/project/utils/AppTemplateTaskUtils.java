package cn.bctools.design.project.utils;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgress;
import cn.bctools.design.project.entity.JvsAppTemplateTaskProgressDetail;
import cn.bctools.redis.utils.RedisUtils;
import com.alibaba.fastjson2.JSON;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jvs
 * 应用模板迭代任务工具
 */
public class AppTemplateTaskUtils {

    private AppTemplateTaskUtils() {
    }

    /**
     * 缓存key前缀
     */
    private static final String TASK_CACHE_PREFIX_KEY = "template:app:progress";

    /**
     * 任务进度缓存key
     */
    private static final String TASK_PROGRESS_KEY = TASK_CACHE_PREFIX_KEY + ":info";

    /**
     * 检查心跳锁
     */
    private static final String CHECK_LOCK = TASK_CACHE_PREFIX_KEY + ":lock";
    /**
     * 检查心跳锁时长
     */
    private static final Integer CHECK_LOCK_TTL = 60;
    /**
     * 缓存项过期时长
     */
    private static final Long TTL = 86400L;

    private static final RedisUtils redisUtils = SpringContextUtil.getBean(RedisUtils.class);


    /**
     * 任务心跳key
     *
     * <p>
     * 数据结构为hash
     * 缓存所有处理中的任务心跳
     * 数据结构：Map<任务标识， 最后一次心跳时间>
     *
     * @return template:app:progress:hart
     */
    public static String getHeartKey() {
        return SysConstant.redisKey(TASK_CACHE_PREFIX_KEY, "heart");
    }

    /**
     * 检查心跳锁key
     */
    public static String getCheckLockKey() {
        return SysConstant.redisKey(CHECK_LOCK, "check");
    }

    /**
     * 任务进度缓存key
     *
     * <p>
     * 数据结构为hash
     * 缓存应用下所有处理中的任务。
     * 数据结构：Map<任务id， 任务>
     *
     * @param appId 应用id
     * @return template:app:progress:info:{应用id}
     */
    public static String getProgressKey(String appId) {
        return SysConstant.redisKey(TASK_PROGRESS_KEY, appId);
    }

    /**
     * 缓存进度的步骤详细信息key
     *
     * <p>
     * 数据结构为hash
     * 缓存应用下所有处理中的任务
     * 数据结构：Map<步骤枚举名， 步骤信息>
     *
     * @param taskId 任务id
     * @return template:app:progress:info:detail:{任务id}
     */
    public static String getProgressDetailKey(String taskId) {
        return SysConstant.redisKey(TASK_PROGRESS_KEY, "detail:" + taskId);
    }


    /**
     * 缓存任务属于那个应用
     *
     * <p>
     * 数据结构为字符串
     * 数据结构：应用id
     *
     * @param taskId 任务id
     * @return template:app:progress:info:appid:{任务id}
     */
    public static String getTaskAppKey(String taskId) {
        return SysConstant.redisKey(TASK_PROGRESS_KEY, "appid:" + taskId);
    }

    /**
     * 缓存用户创建的处理中的迭代任务
     *
     * <p>
     * 数据结构为Set
     * 缓存用户创建的处理中的迭代任务id
     * 数据结构：[任务id]
     *
     * @param userId 用户id
     * @return template:app:progress:info:user:{用户id}
     */
    public static String getUserTaskKey(String userId) {
        return SysConstant.redisKey(TASK_PROGRESS_KEY, "user:" + userId);
    }

    /**
     * 更新心跳
     *
     * @param key             心跳项key
     * @param heartTimeMillis 心跳时间(毫秒)
     */
    public static void updateHeart(String key, long heartTimeMillis) {
        redisUtils.hset(getHeartKey(), key, heartTimeMillis, TTL);
    }


    /**
     * 获取校验心跳锁
     *
     * @param checkLockKey 锁key
     * @return true-获取到锁，false-未获取到锁
     */
    public static boolean tryLockCheckHeart(String checkLockKey) {
        return redisUtils.tryLock(checkLockKey, CHECK_LOCK_TTL);
    }

    /**
     * 获取所有心跳
     *
     * @return 心跳map
     */
    public static Map<Object, Object> getAllHeart() {
        return redisUtils.hmget(getHeartKey());
    }

    /**
     * 删除单个心跳
     *
     * @param key 心跳项key
     */
    public static void removeHeart(String key) {
        // 删除心跳缓存
        redisUtils.hdel(getHeartKey(), key);
    }

    /**
     * 删除多个心跳
     *
     * @param item 待删除的心跳项
     */
    public static void removeHeart(Object... item) {
        redisUtils.hdel(getHeartKey(), item);
    }

    /**
     * 解锁
     *
     * @param lockey 锁key
     */
    public static void unLock(String lockey) {
        redisUtils.unLock(lockey);
    }


    /**
     * 获取任务属于那个应用id
     *
     * @param taskId 任务id
     * @return 应用id
     */
    public static String getTaskAppId(String taskId) {
        return (String) redisUtils.get(getTaskAppKey(taskId));
    }

    /**
     * 获取任务
     *
     * @param appId  应用id
     * @param taskId 任务id
     * @return 任务
     */
    public static JvsAppTemplateTaskProgress getTaskProgress(String appId, String taskId) {
        return Optional.ofNullable(redisUtils.hget(getProgressKey(appId), taskId))
                .map(v -> JSON.parseObject((String) v, JvsAppTemplateTaskProgress.class))
                .orElseGet(() -> null);
    }

    /**
     * 获取指定应用进行中的迭代任务
     *
     * @param appId 应用id
     * @return 进行中的迭代任务
     */
    public static List<JvsAppTemplateTaskProgress> listTaskProgress(String appId) {
        return redisUtils.hmget(getProgressKey(appId)).values()
                .stream()
                .map(value -> JSON.parseObject(value.toString(), JvsAppTemplateTaskProgress.class))
                .sorted(Comparator.comparing(JvsAppTemplateTaskProgress::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取指定进度步骤
     *
     * @param taskId 任务id
     * @param item   项
     * @return 任务
     */
    public static JvsAppTemplateTaskProgressDetail getDetail(String taskId, String item) {
        return JSON.parseObject((String) redisUtils.hget(getProgressDetailKey(taskId), item), JvsAppTemplateTaskProgressDetail.class);
    }

    /**
     * 获取指定任务所有步骤
     *
     * @param taskId 任务id
     * @return 任务所有步骤
     */
    public static Map<Object, Object> listDetail(String taskId) {
        return redisUtils.hmget(getProgressDetailKey(taskId));
    }

    /**
     * 获取用户创建的处理中的迭代任务
     *
     * @param userId 用户id
     * @return 用户创建的处理中的迭代任务id集合
     */
    public static Set<Object> listUserTaskIds(String userId) {
        return redisUtils.sGet(getUserTaskKey(userId));
    }


    /**
     * 缓存任务
     *
     * @param appId  应用id
     * @param taskId 任务id
     * @param task   任务
     */
    public static void cacheProgress(String appId, String taskId, Object task) {
        redisUtils.hset(getProgressKey(appId), taskId, task);
    }

    /**
     * 缓存任务单个进度
     *
     * @param taskId 任务id
     * @param item   项
     * @param task   任务
     */
    public static void cacheProgressDetail(String taskId, String item, Object task) {
        redisUtils.hset(getProgressDetailKey(taskId), item, task);
    }

    /**
     * 缓存任务多个进度
     *
     * @param taskId 任务id
     * @param map    多个进度
     */
    public static void cacheProgressDetails(String taskId, Map<String, Object> map) {
        redisUtils.hmset(getProgressDetailKey(taskId), map);
    }


    /**
     * 缓存任务属于那个应用
     *
     * @param appId  应用id
     * @param taskId 任务id
     */
    public static void cacheTaskAppId(String appId, String taskId) {
        redisUtils.set(getTaskAppKey(taskId), appId);
    }

    /**
     * 缓存用户创建的处理中的迭代任务
     *
     * @param userId 用户id
     * @param taskId 任务id
     */
    public static void cacheUserTaskId(String userId, String taskId) {
        redisUtils.sSet(getUserTaskKey(userId), taskId);
    }


    /**
     * 删除缓存任务
     *
     * @param appId  应用id
     * @param taskId 任务id
     */
    public static void removeProgressCache(String appId, String taskId) {
        redisUtils.hdel(getProgressKey(appId), taskId);
    }

    /**
     * 删除任务属于那个应用
     *
     * @param taskId 任务id
     */
    public static void removeTaskAppId(String taskId) {
        redisUtils.del(getTaskAppKey(taskId));
    }


    /**
     * 删除用户创建的处理中的迭代任务
     *
     * @param userId 用户id
     * @param taskId 任务id
     */
    public static void removeUserTaskId(String userId, String taskId) {
        redisUtils.setRemove(getUserTaskKey(userId), taskId);
    }

    /**
     * 删除进度的步骤详细信息
     *
     * @param taskId 任务id
     */
    public static void removeProgressDetail(String taskId) {
        redisUtils.del(getProgressDetailKey(taskId));
    }
}
