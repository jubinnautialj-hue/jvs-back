package cn.bctools.rule.common;


import cn.bctools.auth.api.api.EnvironmentVariableApi;
import cn.bctools.auth.api.dto.EnvironmentVariableDto;
import cn.bctools.auth.api.enums.EnvironmentVariableEnum;
import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.service.ModelInterface;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 环境变量选择器，如果是实现这个接口的， 那泛型 T 为环境变量的结构，需要添加对应的环境变量才能使用,方便跨环境
 *
 * @author jvs
 */
public interface EnvironmentVariableSelected<T> extends ParameterSelected<T> {

    @Override
    default List<ParameterOption<T>> getOptions() throws BusinessException {
        //这里获取环境变量的数据值
        ModelInterface bean = SpringContextUtil.getBean(ModelInterface.class);
        List<EnvironmentVariableDto> data = bean.getAll();
        List collect = data.stream()
                .filter(e -> e.getType().equals(EnvironmentVariableEnum.key))
                .map(e -> new ParameterOption().setValue(e.getLabel()).setLabel(e.getLabel()))
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    default Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }
}
