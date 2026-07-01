package cn.bctools.design.workflow.model.properties;

import lombok.Data;

/**
 * @author zhuxiaokang
 * 逻辑节点属性配置
 */
@Data
public class AutomationProperties {

    /**
     * 逻辑引擎名称
     */
    private String name;

    /**
     * 逻辑引擎key
     */
    private String key;
}
