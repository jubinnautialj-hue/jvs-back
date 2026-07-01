package cn.bctools.rule.encryption.encryp;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 是否开启数据权限
 *
 * @Author: GuoZi
 */
@Slf4j
@Service
@AllArgsConstructor
public class EnableDataScopeSelected implements ParameterSelected<Boolean> {

    @Override
    public List<ParameterOption<Boolean>> getOptions() {
        return Arrays.asList(
                new ParameterOption<Boolean>().setLabel("开启").setValue(true),
                new ParameterOption<Boolean>().setLabel("关闭").setValue(false)
        );
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(1).getValue();
    }

}
