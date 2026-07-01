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
public class EncryptDecryptSelected implements ParameterSelected<Boolean> {

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

    /**
     * 获取请求方法类型
     *
     * @author: guojing
         * @return: {@linkplain List< ParameterOption> }
     */
    @Override
    public List<ParameterOption<Boolean>> getOptions() {
        return Arrays.asList(new ParameterOption().setLabel("加密").setValue(true), new ParameterOption().setLabel("解密").setValue(false));
    }

}
