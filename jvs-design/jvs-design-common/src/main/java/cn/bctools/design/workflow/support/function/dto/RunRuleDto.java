package cn.bctools.design.workflow.support.function.dto;

import cn.bctools.design.workflow.support.RuntimeData;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 执行逻辑入参
 */
@Data
@Accessors(chain = true)
public class RunRuleDto {

    /***
     * 逻辑引擎key
     */
    private String ruleKey;

    /**
     * 运行时参数
     */
    private RuntimeData runtimeData;
}
