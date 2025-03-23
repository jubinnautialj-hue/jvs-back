package cn.bctools.rule.tools.ftp;

import cn.bctools.rule.common.EnvironmentVariableSelected;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 */
@Slf4j
@Service
@AllArgsConstructor
public class FtpSelected implements EnvironmentVariableSelected<FtpSelectedOption> {
}
