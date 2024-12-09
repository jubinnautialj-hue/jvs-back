package cn.bctools.design.rule.impl.workflow;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.workflow.entity.FlowDesign;
import cn.bctools.design.workflow.model.enums.NodePropertiesTypeEnum;
import cn.bctools.design.workflow.model.enums.NodeTypeEnum;
import cn.bctools.design.workflow.service.FlowDesignService;
import cn.bctools.rule.common.LinkFieldSelected;
import cn.bctools.rule.common.ParameterOption;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guojing
 */
@Slf4j
@Service
@AllArgsConstructor
public class WorkflowLinkFields implements LinkFieldSelected<String> {

    private final FlowDesignService flowDesignService;

    @Override
    public Object link(String id, String field) {
        //如果选的是工作流才有此属性，其它情况是没有的
        FlowDesign byId = flowDesignService.getById(id);
        if (ObjectNull.isNull(byId)) {
            return null;
        }

        List<ParameterOption<String>> optionList = flowDesignService.getByAppId(byId.getJvsAppId(), new String[]{NodeTypeEnum.SP.name()})
                .stream()
                .filter(e -> e.getId().equals(id))
                .flatMap(e -> e.getNodes().stream())
                //添加工作流过滤条件
                .filter(e -> e.getApproverType().equals(NodePropertiesTypeEnum.SELF_SELECT))
                .map(e ->
                        new ParameterOption<String>().setLabel(e.getName()).setValue(e.getId())
                ).collect(Collectors.toList());
        return optionList;
    }

    @Override
    public List<String> fields() {
        return new ArrayList<String>() {{
            add("approvers");
        }};
    }
}
