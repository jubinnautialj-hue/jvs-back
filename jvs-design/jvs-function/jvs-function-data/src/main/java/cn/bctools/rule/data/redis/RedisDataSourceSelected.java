package cn.bctools.rule.data.redis;

import cn.bctools.rule.common.EnvironmentVariableSelected;
import cn.bctools.rule.common.ParameterSelected;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author st
 */
@Slf4j
@Component
public class RedisDataSourceSelected implements EnvironmentVariableSelected<RedisDataSourceSelectedOption> {
}
