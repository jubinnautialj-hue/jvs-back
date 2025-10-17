package cn.bctools.rule.data.mongo;

import cn.bctools.rule.common.EnvironmentVariableSelected;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MongoDBSelected implements EnvironmentVariableSelected<MongoDBOption> {
}
