package cn.bctools.rule.selected;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author guojing
 */
@Service
public class BooleanSelected implements ParameterSelected<Boolean> {

    /**
     * 获取请求方法类型
     *
     * @author: guojing
     * @return {@linkplain List<ParameterOption> }
     */
    @Override
    public List<ParameterOption<Boolean>> getOptions() {
        return Arrays.asList(
                new ParameterOption<Boolean>().setLabel("是").setValue(true),
                new ParameterOption<Boolean>().setLabel("否").setValue(false));
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

}
