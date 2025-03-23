package cn.bctools.data.factory.util;

import cn.bctools.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExcelLock {

    private static final String KEY_FORMAT = "factory:excel:read:lock:";
    private final RedisUtils redisUtils;

    public boolean tryLock(String dataSourceId){
        return redisUtils.tryLock(getKey(dataSourceId),10*60);
    }

    public void unLock(String dataSourceId){
        redisUtils.unLock(getKey(dataSourceId));
    }

    private String getKey(String dataSourceId){
        return KEY_FORMAT+dataSourceId;
    }
}
