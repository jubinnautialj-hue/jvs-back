package cn.bctools.rule.business.user;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Slf4j
@Service
public class SearchUserSelected implements ParameterSelected<String> {

    /**
     * 获取请求方法类型
     *
     * @return {@linkplain List < ParameterOption > }
     * @author: guojing
     */
    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(SearchType.values()).map(e ->
                new ParameterOption<String>().setLabel(e.getMsg()).setValue(e.name())
        ).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

}
