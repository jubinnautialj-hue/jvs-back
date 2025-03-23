package cn.bctools.rule.data.selected;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import cn.bctools.rule.cons.SHEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author jvs
 */
@Slf4j
@Service
@AllArgsConstructor
public class ResultTypeSelected implements ParameterSelected<String> {


    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

    /**
     * 获取请求方法类型
     *
     * @author: jvs
     * @return: {@linkplain List< ParameterOption> }
     */
    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.asList(
                new ParameterOption().setLabel("集合").setShow(SHEnum.vueMessageError).setValue("LIST"),
                new ParameterOption().setLabel("单值").setShow(SHEnum.vueMessageSuccess).setValue("OBJECT"));
    }

}
