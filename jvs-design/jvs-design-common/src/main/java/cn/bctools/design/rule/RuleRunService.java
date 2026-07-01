package cn.bctools.design.rule;

import cn.bctools.design.rule.entity.RuleDesignPo;
import cn.bctools.rule.utils.html.RuleExecuteDto;

import java.util.List;
import java.util.Map;

/**
 * @author wl
 */
public interface RuleRunService {
    /**
     * 执行逻辑引擎,返回对应数据
     *
     * @param key
     * @param dataMap
     * @return
     */
    RuleExecuteDto run(String key, Map<String, Object> dataMap);

    /**
     * 导入逻辑引擎前的检查是否存在循环依赖
     *
     * @param ruleDesignPos
     */
    void importCheck(List<RuleDesignPo> ruleDesignPos);
}
