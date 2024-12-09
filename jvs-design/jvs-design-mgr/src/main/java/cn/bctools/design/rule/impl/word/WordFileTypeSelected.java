package cn.bctools.design.rule.impl.word;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jvs
 */
@Service
public class WordFileTypeSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        List<ParameterOption<String>> list = new ArrayList<>();
        list.add(new ParameterOption<String>().setLabel("docx").setValue("docx"));
        list.add(new ParameterOption<String>().setLabel("pdf").setValue("pdf"));
        return list;
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }

}
