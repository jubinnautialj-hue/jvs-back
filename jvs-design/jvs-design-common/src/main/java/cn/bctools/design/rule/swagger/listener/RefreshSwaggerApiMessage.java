package cn.bctools.design.rule.swagger.listener;

import lombok.Data;

/**
 * @author jvs
 * 刷新swagger api缓存MQ消息
 */
@Data
public class RefreshSwaggerApiMessage {
    /**
     * true-删除操作, false-非删除操作
     */
    private Boolean delete;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 逻辑id
     */
    private String ruleId;

    /**
     * 模式
     */
    private String mode;
}
