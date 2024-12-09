package cn.bctools.auth.service;

import cn.bctools.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xl
 */
@Slf4j
@Service
public class InitConfigService {
    @Autowired
    RedisUtils redisUtils;

    /**
     * 刷新配置
     */
    public void refresh() {
        redisUtils.publish("gatewayConfig", null);
    }
}
