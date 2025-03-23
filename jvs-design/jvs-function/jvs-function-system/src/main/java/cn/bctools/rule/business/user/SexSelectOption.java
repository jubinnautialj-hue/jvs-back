package cn.bctools.rule.business.user;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class SexSelectOption implements ParameterSelected<String> {

    /**
     * 获取请求方法类型
     *
     * @return {@linkplain List < ParameterOption > }
     */
    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.asList(
                new ParameterOption<String>().setLabel("男").setValue("男"),
                new ParameterOption<String>().setLabel("女").setValue("女"),
                new ParameterOption<String>().setLabel("保密").setValue("保密")
        );
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }
}
