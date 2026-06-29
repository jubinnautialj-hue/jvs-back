package cn.bctools.document.util;


import cn.bctools.redis.utils.RedisUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 分布式锁工具
 */
@AllArgsConstructor
@Component
public class RedisLockUtil {

    private RedisUtils redisUtils;
    private final static String QUEUE_FILE_TO_ES_TASK_LOCK = "document:lock:queue:fileToEs:%s_%s";

    /**
     * 文件同步es消息队列锁
     */
    public Boolean GetQueueFileToEsTaskLock(String id, String tenantId) {
        return redisUtils.tryLock(String.format(QUEUE_FILE_TO_ES_TASK_LOCK, id, tenantId), 1800);
    }


    /**
     * 文件同步es消息队列锁
     */
    public void UnLockQueueFileToEsTaskLock(String id, String tenantId) {
        redisUtils.unLock(String.format(QUEUE_FILE_TO_ES_TASK_LOCK, id, tenantId));
    }
}
