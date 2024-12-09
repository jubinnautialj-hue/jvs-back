package cn.bctools.log.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuxiaokang
 * 日志MQ配置
 */
@Configuration
public class LogMqConfig {

    public static final String SYS_LOG_SAVE_EXCHANGE = "jvs-saveSysLog";
    public static final String SYS_LOG_SAVE_QUEUE = "jvs-saveSysLog-queue";
    public static final String SYS_LOG_SAVE_ROUTING = "jvs-saveSysLog-routing";

    @Bean
    public TopicExchange sysLogSaveExchange(){
        Map<String, Object> pros = new HashMap<>(1);
        return new TopicExchange(SYS_LOG_SAVE_EXCHANGE, true, false, pros);
    }

    @Bean
    public Queue sysLogSaveQueue(){
        return QueueBuilder.durable(SYS_LOG_SAVE_QUEUE).build();
    }

    @Bean
    public Binding sysLogSaveBinding(){
        return BindingBuilder.bind(sysLogSaveQueue()).to(sysLogSaveExchange()).with(SYS_LOG_SAVE_ROUTING);
    }
}
