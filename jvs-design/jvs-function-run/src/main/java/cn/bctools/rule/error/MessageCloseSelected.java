package cn.bctools.rule.error;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


/**
 * @author jvs
 * The type Message close selected.
 */
@Slf4j
@Service
@AllArgsConstructor
public class MessageCloseSelected implements ParameterSelected<Boolean> {


    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

    /**
     * 获取请求方法类型
     *
     * @author: gx
     * @return: {@linkplain List <  ParameterOption > }
     */
    @Override
    public List<ParameterOption<Boolean>> getOptions() {
        return Arrays.asList(
                new ParameterOption<Boolean>().setLabel("是").setValue(true),
                new ParameterOption<Boolean>().setLabel("否").setValue(false));
    }

}
