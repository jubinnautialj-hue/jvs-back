package cn.bctools.rule.data.dm;

import cn.bctools.rule.common.EnvironmentVariableSelected;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 获取数据源集
 *
 * @author jvs
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DmEnvironmentVariableSelected implements EnvironmentVariableSelected<DMDatasourceSelectedOption> {
}
