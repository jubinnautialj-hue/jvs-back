package cn.bctools.design.workflow.model.properties;

import cn.bctools.design.workflow.enums.BackScopeEnum;
import cn.bctools.design.workflow.enums.BackTaskResubmitEnum;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 回退配置
 */
@Data
public class BackProperties {

    /**
     * 回退范围
     */
    private BackScopeEnum scope = BackScopeEnum.APPROVED;

    /**
     * 回退后重新发起规则
     */
    private BackTaskResubmitEnum resubmit = BackTaskResubmitEnum.SEQUENCE;
}
