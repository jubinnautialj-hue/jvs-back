package cn.bctools.design.rule.service;

import cn.bctools.design.rule.entity.RuleDesignPo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;


/**
 * The interface Rule design service.
 *
 * @author guojing
 */
public interface RuleDesignService extends IService<RuleDesignPo> {
    /**
     * 获取这个设计类型
     *
     * @param secret secret  调用逻辑的平整
     * @return enable design
     */
    RuleDesignPo getEnableDesign(String secret);

    /**
     * 根据应用Id查询关联的工作流的引用 关系
     * 避免相互递归调用死循环
     *
     * @param jvsAppId 应用Id
     * @return 返回 map
     */
    Map<? extends String, ? extends List<String>> ruleFlows(String jvsAppId);

}
