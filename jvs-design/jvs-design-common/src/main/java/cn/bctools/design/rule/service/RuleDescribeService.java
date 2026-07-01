package cn.bctools.design.rule.service;

import cn.bctools.design.rule.entity.RunDescribePo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jvs
 * 逻辑的描述信息
 * The interface Rule describe service.
 */
public interface RuleDescribeService extends IService<RunDescribePo> {


    /**
     * Delete rule describe.
     * 删除这个逻辑所有的描述
     *
     * @param ruleKey the rule key
     * @param appId   the app id
     */
    void deleteRuleDescribe(String ruleKey, String appId);

}
