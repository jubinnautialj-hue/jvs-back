package cn.bctools.design.workflow.support.listener.asynctask;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 同步流程数据到业务MQ配置
 */
@Configuration
public class AsyncTaskDynamicDataMqConfig {
    public static final String ASYNC_TASK_DATA_EXCHANGE = "jvs-asyncTaskToDynamicData";
    public static final String ASYNC_TASK_DATA_QUEUE = "jvs-asyncTaskToDynamicData-queue";
    public static final String ASYNC_TASK_DATA_ROUTING = "jvs-asyncTaskToDynamicData-routing";

    @Bean
    public TopicExchange asyncTaskDataExchange(){
        Map<String, Object> pros = new HashMap<>(1);
        return new TopicExchange(ASYNC_TASK_DATA_EXCHANGE, true, false, pros);
    }

    @Bean
    public Queue asyncTaskDataQueue(){
        return QueueBuilder.durable(ASYNC_TASK_DATA_QUEUE).build();
    }

    @Bean
    public Binding asyncTaskDataBinding(){
        return BindingBuilder.bind(asyncTaskDataQueue()).to(asyncTaskDataExchange()).with(ASYNC_TASK_DATA_ROUTING);
    }
}
