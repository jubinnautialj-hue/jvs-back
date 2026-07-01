package cn.bctools.rule.business.dept;

import cn.bctools.common.enums.DeptEnum;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author jvs
 */
@Slf4j
@Service
public class DeptEnumUpdateSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.asList(
                new ParameterOption<String>().setLabel("部门").setValue(DeptEnum.dept.name()),
                new ParameterOption<String>().setLabel("子公司").setValue(DeptEnum.branchOffice.name()));
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

}
