package cn.bctools.document;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.constant.Constant;
import cn.bctools.oauth2.annotation.EnableJvsMgrResourceServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Set;

/**
 * @author guojing
 */
@RefreshScope
@EnableAsync
@EnableJvsMgrResourceServer
@EnableCaching
@EnableDiscoveryClient
@SpringBootApplication
@Slf4j
public class DocumentMgrApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentMgrApplication.class, args);
//        //删除网络原因造成的缓存数据没有清理
//        RedisTemplate redisTemplate = SpringContextUtil.getBean(RedisTemplate.class);
//        Set<String> keys = redisTemplate.keys(String.format(Constant.UPDATE_FOLDER, "*"));
//        for (String key : keys) {
//            redisTemplate.delete(key);
//        }
    }
}

