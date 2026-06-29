package cn.bctools.web.config;

import cn.bctools.common.utils.SpringContextUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.function.context.FunctionProperties;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jvs
 */
@Component
public class BeanPostProcessorConfig extends SpringContextUtil implements BeanPostProcessor, ApplicationContextAware {
    /**
     * 设置不合规的bean，可避免被spring自动注册到MQ
     */
    List<String> ineligibleDefinitions = Stream.of("inMemorySwaggerResourcesProvider").collect(Collectors.toList());

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        setIneligibleDefinitions(bean);
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    /**
     * 设置不合规的bean
     */
    private void setIneligibleDefinitions(Object bean) {
        if (bean instanceof FunctionProperties) {
            FunctionProperties functionProperties = applicationContext.getBean(FunctionProperties.class);
            functionProperties.setIneligibleDefinitions(ineligibleDefinitions);
        }
    }
}
