package cn.bctools.rule.data.selected;

import cn.bctools.rule.common.EnvironmentVariableSelected;
import cn.bctools.rule.data.mysql.DatasourceSelectedOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 环境变量的处理
 * @author jvs
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class MysqlEnvironmentVariableSelected implements EnvironmentVariableSelected<DatasourceSelectedOption> {

}
