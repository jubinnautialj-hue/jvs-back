package org.jim.core.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * 服务上下文工具类，可直接操作此工具类获取版本号，服务名，环境和Spring 管理的Bean对象,在Aop、日志、组件重写等地方频繁使用
 *
 * @author guojing
 * @describe
 */
@Slf4j
@EnableAsync
@Order(0)
@Lazy(false)
@Component
public class SpringContextUtils implements ApplicationContextAware {

    /**
     * 公共Spring上下文，默认为空，等项目启动后会进行初始化成功
     */
    protected static ApplicationContext applicationContext = null;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * {@value applicationContextName} 直接获取系统的命名默认获取规则为 spring.application.name 方式，由于此配置默认使用@project.artifactId@  ,所以会直接使用pom中的项目名
     */
    @Getter
    protected static String applicationContextName = "";
    /**
     * {@value env} 直接获取系统的命名默认获取规则为 spring.profiles.active 方式，由于此配置默认使用@profiles.active@  ,所以会直接使用打包的时候的环境,或由项目启动时指定 目前已经设置有dev|sit|uat|beta|pro五个环境，实际情况根据项目来
     */
    @Getter
    protected static String env = "";
    /**
     * {@value version} 直接获取系统的命名默认获取规则为 project.version 方式，由于此配置默认使用@project.version@  ,所以会直接使用pom中的项版本号
     * 业务系统版本号
     */
    @Getter
    protected static String version = "";

    /**
     * 公共工具，可直调用此方法直接获取任何Spring管理的Bean对象，可以获取Mapper  Service  Component Configuration等
     *
     * @param var Bean的Class
     * @author: guojing
     * @return: T 返回实体的Bean对象，并可直接调用其方法
     */
    public static <T> T getBean(Class<T> var) {
        return applicationContext.getBean(var);
    }

    /**
     * 重写Bean，主要为了初始化公共 {@linkplain ApplicationContext} 和初始化环境和名称对象
     *
     * @param context {@linkplain ApplicationContext}
     * @return: void
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        applicationContextName = context.getEnvironment().getProperty("spring.application.name");
        String port = context.getEnvironment().getProperty("server.port");
        String en = context.getEnvironment().getProperty("spring.profiles.active");
        String namespace = context.getEnvironment().getProperty("spring.cloud.nacos.discovery.namespace");
        String envformat = "[%s]-[%s]-[%s]-[%s]-[%s]";
        version += context.getEnvironment().getProperty("project.version");
        env = String.format(envformat, applicationContextName, version, port, en, namespace);
    }

}
