package cn.bctools.design.rule.swagger;

import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import cn.bctools.design.rule.entity.RuleDesignPo;

/**
 * @author jvs
 * 将逻辑引擎设计解析为swagger可查看的接口
 */
public interface SwaggerRuleApiCacheService {
    /**
     * 初始化所有逻辑API填充swagger缓存
     */
    void initAllSwaggerRuleApi();

    /**
     * 发布刷新逻辑设计到swagger任务
     *
     * @param delete true-删除接口
     * @param appId  应用id
     */
    void publishSwaggerRuleApiEvent(Boolean delete, String appId);

    /**
     * 发布刷新逻辑设计到swagger任务
     *
     * @param mode   模式
     * @param delete true-删除接口
     * @param appId  应用id
     */
    void publishSwaggerRuleApiEvent(AppVersionTypeEnum mode, Boolean delete, String appId);

    /**
     * 发布刷新逻辑设计到swagger任务
     *
     * @param delete true-删除接口
     * @param appId  应用id
     * @param rule   指定要刷新的逻辑设计，不指定，则刷新应用下所有逻辑接口
     */
    void publishSwaggerRuleApiEvent(Boolean delete, String appId, RuleDesignPo rule);

}
