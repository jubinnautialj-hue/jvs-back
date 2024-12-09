package cn.bctools.design.rule.impl.rule.ergodic;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jvs
 */
@Service
@AllArgsConstructor
public class ErgodicAsyncSelected implements ParameterSelected<ErgodicAsyncEnum> {

    @Override
    public List<ParameterOption<ErgodicAsyncEnum>> getOptions() {
        return Arrays.stream(ErgodicAsyncEnum.values()).map(e -> new ParameterOption<ErgodicAsyncEnum>().setLabel(e.getMsg()).setValue(e)).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }
}
