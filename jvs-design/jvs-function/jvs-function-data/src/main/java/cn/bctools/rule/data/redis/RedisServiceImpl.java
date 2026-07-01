package cn.bctools.rule.data.redis;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.service.ModelInterface;
import cn.hutool.core.lang.Validator;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Service
@Rule(value = "Redis",
        group = RuleGroup.数据插件,
        test = true,
        enable = false,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 20,
//        iconUrl = "rule-redisyunshujukuRedisban",
        explain = "redis是一个key-value存储系统。和Memcached类似，它支持存储的value类型相对更多，包括string(字符串)、list(链表)、set(集合)、zset(sorted set --有序集合)和hash（哈希类型）。"
)
public class RedisServiceImpl implements BaseCustomFunctionInterface<RedisFunctionDto> {
    @Autowired
    ModelInterface modelInterface;

    /**
     * StringRedisTemplate实例缓存（ConcurrentHashMap<host, RedisTemplateCache>）
     */
    private ConcurrentHashMap<String, RedisTemplateCache> redisTemplateCacheGlobal = new ConcurrentHashMap<>();

    /**
     * 当前线程使用的StringRedisTemplate实例
     */
    private ThreadLocal<StringRedisTemplate> redisTemplate = new ThreadLocal<>();

    /**
     * 通过逻辑引擎设值，redis的key统一前缀"rule:module:"
     */
    private static final String DESIGN_RULE_REDIS_PREFIX = "rule:module:";

    /**
     * 通过逻辑引擎设值，默认数据有效时长30天。
     */
    private static final Long DEFAULT_EXPIRE = 30 * 24 * 60 * 60L;

    @Override
    public Object execute(RedisFunctionDto dto, Map<String, Object> params) {
        Object byKey = modelInterface.getByKey(dto.getDbName());
        RedisDataSourceSelectedOption sourceSelectedOption = BeanCopyUtil.copy(RedisDataSourceSelectedOption.class, byKey);
        dto.setOption(sourceSelectedOption);
        // 参数校验
        checkParam(dto);
        // redis自动配置
        createRedisTemplate(dto);
        // 操作缓存
        Object obj = operationRedis(dto);
        removeRedisTemplate();
        return obj;
    }


    /**
     * 参数校验
     *
     * @param dto
     */
    private void checkParam(RedisFunctionDto dto) {
        Validator.validateBetween(dto.getOption().getDatabase(), 0, 15, "数据库实例可选范围[0-15]");
        if (!RedisOperationTypeEnum.GET.equals(dto.getOperationType()) && !dto.getKey().startsWith(DESIGN_RULE_REDIS_PREFIX)) {
            throw new BusinessException("只能保存或删除，key前缀为\"rule:module:\"的数据");
        }
    }

    /**
     * 操作redis数据
     *
     * @param dto 入参
     * @return
     */
    private Object operationRedis(RedisFunctionDto dto) {
        String key = dto.getKey() + Optional.ofNullable(dto.getKeySuffix()).orElse("");
        switch (dto.getOperationType()) {
            case GET:
                return getRedisExecute(key);
            case SET:
                return setRedisExecute(key, dto.getValue(), Optional.ofNullable(dto.getTtl()).orElse(DEFAULT_EXPIRE));
            case DEL:
                return delRedisExecute(key);
            default:
                throw new BusinessException("不支持的操作类型");
        }
    }

    /**
     * 取值
     *
     * @param key
     * @return
     */
    private Object getRedisExecute(String key) {
        // 判断key是否存在，若不存在，则直接返回空
        if (Boolean.FALSE.equals(redisTemplate.get().hasKey(key))) {
            return "";
        }
        DataType dataType = Objects.requireNonNull(redisTemplate.get().type(key));
        // 根据key的类型，获取数据
        switch (dataType) {
            case STRING:
                return redisTemplate.get().boundValueOps(key).get();
            case HASH:
                return BeanCopyUtil.copy(redisTemplate.get().opsForHash().entries(key), Object.class);
            case LIST:
                return Optional.ofNullable(redisTemplate.get().opsForList().range(key, 0, -1))
                        .map(data -> data.stream().map(e -> JSON.parseObject(e, Object.class)).collect(Collectors.toList()))
                        .orElse(Collections.emptyList());
            case SET:
                return Optional.of(redisTemplate.get().opsForSet().members(key))
                        .map(data -> data.stream().map(e -> JSON.parseObject(e, Object.class)).collect(Collectors.toSet()))
                        .orElse(Collections.emptySet());
            case ZSET:
                return Optional.of(redisTemplate.get().opsForZSet().rangeByScore(key, Double.MIN_VALUE, Double.MAX_VALUE))
                        .map(data -> data.stream().map(e -> JSON.parseObject(e, Object.class)).collect(Collectors.toSet()))
                        .orElse(Collections.emptySet());
            default:
                throw new BusinessException("不支持的redis数据类型");
        }
    }

    /**
     * 设值
     *
     * @param key
     * @param value
     * @param expire
     * @return
     */
    private Object setRedisExecute(String key, Object value, long expire) {
        redisTemplate.get().opsForValue().set(key, JSON.toJSONString(value), expire, TimeUnit.SECONDS);
        return value;
    }

    /**
     * 删除
     *
     * @param key
     * @return
     */
    private Object delRedisExecute(String key) {
        if (Boolean.TRUE.equals(redisTemplate.get().hasKey(key))) {
            redisTemplate.get().delete(key);
        }
        return "";
    }

    /**
     * 创建Redis template实例
     *
     * @param reqDto
     */
    public void createRedisTemplate(RedisFunctionDto reqDto) {
        RedisAutoConfig redisAutoConfig = new RedisAutoConfig();
        RedisProperties redisProperties = redisAutoConfig.initRedisProperties(reqDto);

        String cacheKey = redisProperties.getHost();
        // 判断是否创建StringRedisTemplate实例
        RedisTemplateCache redisTemplateCache = redisTemplateCacheGlobal.get(cacheKey);
        String configMd5Str = MD5.create().digestHex(JSON.toJSONString(redisProperties));
        if (redisTemplateCache != null && redisTemplateCache.getRedisConfigMd5().equals(configMd5Str)) {
            // 若StringRedisTemplate实例已存在且配置没有变更，则直接返回
            setRedisTemplate(redisTemplateCache.getStringRedisTemplate());
            return;
        }

        // 创建StringRedisTemplate实例，并保存到本地缓存
        StringRedisTemplate stringRedisTemplate = redisAutoConfig.connectionRedis(redisProperties);
        redisTemplateCacheGlobal.put(cacheKey, new RedisTemplateCache(configMd5Str, stringRedisTemplate));
        setRedisTemplate(stringRedisTemplate);
    }


    /**
     * redis自动配置
     */
    private static class RedisAutoConfig {
        private RedisProperties initRedisProperties(RedisFunctionDto reqDto) {
            int database = Optional.ofNullable(reqDto.getOption().getDatabase()).orElse(0);
            RedisProperties redisProperties = new RedisProperties();
            redisProperties.setHost(reqDto.getOption().getHost());
            redisProperties.setPort(Optional.ofNullable(reqDto.getOption().getPort()).orElse(6379));
            redisProperties.setDatabase(database);
            redisProperties.setPassword(redisProperties.getPassword());
            redisProperties.setTimeout(Duration.ofMillis(10000));
            return redisProperties;
        }

        private StringRedisTemplate connectionRedis(RedisProperties properties) {
            LettuceClientConfiguration clientConfiguration = builderLettuce(properties);
            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(getStandaloneConfig(properties), clientConfiguration);
            lettuceConnectionFactory.afterPropertiesSet();
            return new StringRedisTemplate(lettuceConnectionFactory);
        }

        private LettuceClientConfiguration builderLettuce(RedisProperties properties) {
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
            if (properties.isSsl()) {
                builder.useSsl();
            }
            if (properties.getTimeout() != null) {
                builder.commandTimeout(properties.getTimeout());
            }
            if (properties.getLettuce() != null) {
                RedisProperties.Lettuce lettuce = properties.getLettuce();
                if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                    builder.shutdownTimeout(properties.getLettuce().getShutdownTimeout());
                }
            }
            if (StringUtils.hasText(properties.getClientName())) {
                builder.clientName(properties.getClientName());
            }
            return builder.build();
        }

        private RedisStandaloneConfiguration getStandaloneConfig(RedisProperties properties) {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(properties.getHost());
            config.setPort(properties.getPort());
            config.setPassword(RedisPassword.of(properties.getPassword()));
            config.setDatabase(properties.getDatabase());
            return config;
        }
    }


    /**
     * StringRedisTemplate缓存实体
     */
    private static class RedisTemplateCache {
        // redis配置生成的MD5
        private String redisConfigMd5;

        // StringRedisTemplate实例
        private StringRedisTemplate stringRedisTemplate;

        public RedisTemplateCache(String redisConfigMd5, StringRedisTemplate stringRedisTemplate) {
            this.redisConfigMd5 = redisConfigMd5;
            this.stringRedisTemplate = stringRedisTemplate;
        }

        public String getRedisConfigMd5() {
            return redisConfigMd5;
        }

        public StringRedisTemplate getStringRedisTemplate() {
            return stringRedisTemplate;
        }
    }

    private void setRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        redisTemplate.set(stringRedisTemplate);
    }

    private void removeRedisTemplate() {
        redisTemplate.remove();
    }
}
