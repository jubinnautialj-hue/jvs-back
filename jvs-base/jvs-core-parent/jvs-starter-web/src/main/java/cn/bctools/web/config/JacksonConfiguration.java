package cn.bctools.web.config;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * JacksonConfig
 *
 * @author guojing
 */
@Configuration
@Slf4j
@ConditionalOnClass(ObjectMapper.class)
public class JacksonConfiguration {

    /**
     * 监听器: 监听HTTP请求事件
     * 解决RequestContextHolder.getRequestAttributes()空指针问题
     */
    @Bean
    @ConditionalOnMissingBean(RequestContextListener.class)
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            builder.locale(Locale.CHINA);
            builder.timeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            builder.simpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
            // 解凳循环引用问题：禁用循环引用检测，避免StackOverflowError
            builder.featuresToDisable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
            log.info("[Jackson配置] 已禁用循环引用检测，避免树形结构序列化StackOverflowError");
        };
    }

}
