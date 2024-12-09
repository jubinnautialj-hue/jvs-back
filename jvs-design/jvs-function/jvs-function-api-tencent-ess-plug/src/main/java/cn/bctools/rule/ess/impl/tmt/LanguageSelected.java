package cn.bctools.rule.ess.impl.tmt;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jvs
 */
@Service
public class LanguageSelected implements ParameterSelected<String> {
    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0);
    }

    @Override
    public List<ParameterOption<String>> getOptions() {
        List<ParameterOption<String>> objects = new ArrayList<>();
        objects.add(new ParameterOption<String>("en","英语"));
        objects.add(new ParameterOption<String>("zh","中文"));
        objects.add(new ParameterOption<String>("ja","日语"));
        objects.add(new ParameterOption<String>("ko","韩语"));
        objects.add(new ParameterOption<String>("fr","法语"));
        objects.add(new ParameterOption<String>("es","西班牙语"));
        objects.add(new ParameterOption<String>("it","意大利语"));
        objects.add(new ParameterOption<String>("de","德语"));
        objects.add(new ParameterOption<String>("tr","土耳其语"));
        objects.add(new ParameterOption<String>("ru","俄语"));
        objects.add(new ParameterOption<String>("pt","葡萄牙语"));
        objects.add(new ParameterOption<String>("vi","越南语"));
        objects.add(new ParameterOption<String>("id","印尼语"));
        objects.add(new ParameterOption<String>("th","泰语"));
        objects.add(new ParameterOption<String>("ms","马来语"));
        objects.add(new ParameterOption<String>("ar","阿拉伯语"));
        objects.add(new ParameterOption<String>("hi","印地语"));
        return objects;
    }
}
