package cn.bctools.rule.data.oracle;

import cn.bctools.rule.common.EnvironmentVariableSelected;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 获取数据源集
 *
 * @author guojing
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OracleSelected implements EnvironmentVariableSelected<OracleSelectedOption> {

}
