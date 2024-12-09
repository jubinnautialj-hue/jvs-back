package cn.bctools.rule.error;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import cn.bctools.rule.cons.SHEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class MessageTipsSelected implements ParameterSelected<Boolean> {


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
        return Arrays.asList(
                new ParameterOption().setLabel("成功提示").setShow(SHEnum.vueMessageSuccess).setValue(true),
                new ParameterOption().setLabel("失败提示").setShow(SHEnum.vueMessageError).setValue(false));
    }

}
