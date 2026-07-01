package cn.bctools.design.rule.swagger.listener;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author jvs
 * 刷新swagger api缓存事件
 */
@Getter
@Setter
public class RefreshRuleSwaggerApiEvent extends ApplicationEvent {
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

    public RefreshRuleSwaggerApiEvent(Object source, Boolean delete, String appId, String ruleId, String mode) {
        super(source);
        this.delete = delete;
        this.appId = appId;
        this.ruleId = ruleId;
        this.mode = mode;
    }
}
