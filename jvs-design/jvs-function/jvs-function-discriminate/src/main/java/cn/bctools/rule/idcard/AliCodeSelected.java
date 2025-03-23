package cn.bctools.rule.idcard;

import cn.bctools.rule.common.EnvironmentVariableSelected;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class AliCodeSelected implements EnvironmentVariableSelected<AliCodeOption> {
}
