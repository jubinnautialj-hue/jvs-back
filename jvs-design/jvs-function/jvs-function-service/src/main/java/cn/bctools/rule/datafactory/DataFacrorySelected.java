package cn.bctools.rule.datafactory;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import cn.hutool.core.lang.Dict;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DataFacrorySelected implements ParameterSelected<String> {

//    //DataFactoryApi dataFactoryApi;

    @Override
    public String key() {
        return "jvsAppId";
    }

    @Override
    public List<ParameterOption<String>> getOptions() {
//        List<ParameterOption<String>> collect = new ArrayList<>();
//        List<Dict> data = dataFactoryApi.getAll(null).getData();
//        if (ObjectNull.isNull(data)) {
            return new ArrayList<>();
//        }
//        collect = data
//                .stream()
//                .map(e -> {
//                    ParameterOption<String> stringParameterOption = new ParameterOption<>();
//                    BeanCopyUtil.copy(e, stringParameterOption);
//                    return stringParameterOption;
//                }).collect(Collectors.toList());
//        return collect;
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }

}
