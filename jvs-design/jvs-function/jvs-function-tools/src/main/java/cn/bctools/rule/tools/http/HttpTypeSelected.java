package cn.bctools.rule.tools.http;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import cn.hutool.http.Method;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Service
public class HttpTypeSelected implements ParameterSelected<Method> {

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

    /**
     * 获取请求方法类型
     *
     * @return {@linkplain List<ParameterOption>}
     * @author: guojing
     */
    @Override
    public List<ParameterOption<Method>> getOptions() {
        return Arrays.stream(Method.values()).map(e -> new ParameterOption<Method>().setLabel(e.name()).setValue(e)).collect(Collectors.toList());
    }

}
