package cn.bctools.design.workflow.model.properties;

import cn.bctools.design.workflow.model.enums.TimeLimitTypeEnum;
import lombok.Data;

/**
 * @author zhuxiaokang
 * 审批期限
 */
@Data
public class TimeLimit {

    /**
     * 审批期限单位
     */
    private TimeLimitTypeEnum type;

    /**
     * 期限值（0-无限期）
     */
    private Integer limit;

    /**
     * 超过审批期限的执行事件
     */
    private TimeLimitEvent event;
}
