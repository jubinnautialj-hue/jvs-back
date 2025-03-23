package cn.bctools.rule.data.mysql;

import cn.bctools.rule.common.ParameterSelected;
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
public class DatasourceSelected implements ParameterSelected<DatasourceSelectedOption> {

}
