package cn.bctools.design.rule.impl.workflow.run;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class FlowRunParameterSelected implements ParameterSelected<String> {

    FlowDesignService flowDesignService;

    @Override
    public String key() {
        return "jvsAppId";
    }

    @Override
    public List<ParameterOption<String>> getOptions() {
        //应用ID 必须要从前端 获取 如果获取 不到，即返回错误不可使用
        String appId = SystemThreadLocal.get(key());
        if (ObjectNull.isNull(appId)) {
            throw new BusinessException("请先创建流程");
        }
        return flowDesignService.getByAppId(appId, new String[]{NodeTypeEnum.AUTOMATION.name()})
                .stream()
                .map(e ->
                                new ParameterOption<String>()
//                                .setType(WorkflowEnum.flow.name())
                                        .setLabel(e.getName()).setValue(e.getId())
                ).collect(Collectors.toList());
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }

}
