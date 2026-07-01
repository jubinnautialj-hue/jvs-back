package cn.bctools.rule.data.mongo;

import cn.bctools.rule.common.EnvironmentVariableSelected;
import cn.bctools.rule.common.ParameterSelected;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author st
 */
@Slf4j
@Service
public class MongodbDataSourceSelected implements EnvironmentVariableSelected<MongodbDataSourceSelectedOption> {
}
