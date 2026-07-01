package cn.bctools.design.workflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhuxiaokang
 * 被回退的数据重新提交配置
 */
@Getter
@AllArgsConstructor
public enum BackTaskResubmitEnum {
    /**
     * 按流程顺序审批
     */
    SEQUENCE,
    /**
     * 直达当前（发起回退的）节点
     */
    DIRECT_CURRENT_NODE,
    ;
}
