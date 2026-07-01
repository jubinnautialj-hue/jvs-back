package cn.bctools.design.rule.service;

import cn.bctools.design.rule.entity.RuleDesignPo;

/**
 * @author jvs
 */
public interface RuleDesignFacadeService {

    /**
     * 修改设计
     *
     * @param ruleDesignPo 设计
     * @param appId 应用id
     */
    void update(RuleDesignPo ruleDesignPo, String appId);

}
