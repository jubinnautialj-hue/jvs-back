package cn.bctools.design.workflow.support.timelimit;

/**
 * @author zhuxiaokang
 * 发布审批期限超时校验消息MQ配置
 */
public class TimeLimitMqConfig {

    /**
     * 函数名
     */
    public static final String TIME_LIMIT_FUNCTION = "flowApprovalTimeout";

    /**
     * 生产者
     */
    public static final String TIME_LIMIT_PRODUCER = TIME_LIMIT_FUNCTION + "-out-0";
}
