package cn.bctools.rule.encryption.encryp;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Service
public class EncryptDecryptConsSelected implements ParameterSelected<String> {

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
    public List<ParameterOption<String>> getOptions() {
        return Arrays.stream(EncryptionEnum.values()).map(e -> new ParameterOption<String>().setLabel(e.name()).setValue(e.name())).collect(Collectors.toList());
    }

}
