package cn.bctools.design.rule.impl;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.design.rule.service.RuleDesignService;
import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import cn.bctools.rule.entity.enums.NodeType;
import cn.bctools.rule.utils.html.HtmlGraph;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author wl
 */
@Slf4j
@Service
@AllArgsConstructor
public class NodeSelected implements ParameterSelected<String> {

    RuleDesignService ruleDesignService;

    @Override
    public String key() {
        return "id";
    }

    @Override
    public Object getDefaultValueParameter() {
        return getOptions().get(0).getValue();
    }

    @Override
    public List<ParameterOption<String>> getOptions() {
        String id = SystemThreadLocal.get(key());
        if (ObjectNull.isNull(id)) {
            return new ArrayList<>();
        }
        RuleDesignPo ruleDesignPo = ruleDesignService.getOne(Wrappers.query(new RuleDesignPo().setSecret(id)));
        HtmlGraph graph = JSONObject.parseObject(ruleDesignPo.getDesignDrawingJson(), HtmlGraph.class);
        return graph.getNodeList().stream().filter(e -> e.getType().equals(NodeType.task)).map(e -> new ParameterOption<String>().setLabel(e.getName()).setValue(e.getId())).collect(Collectors.toList());
    }

}
