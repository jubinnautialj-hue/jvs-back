package cn.bctools.rule.data.mysql;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.data.redis.RedisDataSourceSelectedOption;
import cn.bctools.rule.data.redis.RedisFunctionDto;
import cn.bctools.rule.data.redis.RedisOperationTypeEnum;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.hutool.core.lang.Validator;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Rule(value = "Mysql",
        group = RuleGroup.数据插件,
        test = true,
        enable = false,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 20,
//        iconUrl = "rule-redisyunshujukuRedisban",
        explain = "Mysql"
)
public class MysqlServiceImpl implements BaseCustomFunctionInterface<MysqlFunctionDto> {

    @Override
    public Object execute(MysqlFunctionDto mysqlFunctionDto, Map<String, Object> params) {
        return null;
    }
}
