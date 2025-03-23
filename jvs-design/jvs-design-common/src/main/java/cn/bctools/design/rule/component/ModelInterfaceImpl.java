package cn.bctools.design.rule.component;

import cn.bctools.auth.api.api.EnvironmentVariableApi;
import cn.bctools.auth.api.dto.EnvironmentVariableDto;
import cn.bctools.auth.api.enums.ModeTypeEnum;
import cn.bctools.design.util.ModeUtils;
import cn.bctools.rule.service.ModelInterface;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author jvs
 */
@Component
@AllArgsConstructor
public class ModelInterfaceImpl implements ModelInterface {

    EnvironmentVariableApi environmentVariableApi;

    @Override
    public List<EnvironmentVariableDto> getAll() {
        return environmentVariableApi.getAll(ModeTypeEnum.getType(ModeUtils.getMode().getValue())).getData();
    }

    @Override
    public Object getByKey(String label) {
        return environmentVariableApi.getByKey(label, ModeTypeEnum.getType(ModeUtils.getMode().getValue())).getData().getValue();
    }

}
