package cn.bctools.common.utils;

import cn.bctools.common.constant.SysConstant;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.*;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

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
public class SpringContextUtil implements ApplicationContextAware {
    /**
     * 公共Spring上下文，默认为空，等项目启动后会进行初始化成功
     */
    protected static ApplicationContext applicationContext = null;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * {@value applicationContextName} 直接获取系统的命名默认获取规则为 spring.application.name
     * 方式，由于此配置默认使用@project.artifactId@ ,所以会直接使用pom中的项目名
     */
    @Getter
    protected static String applicationContextName = "";
    /**
     * 平台名称
     */
    @Getter
    protected static String platformName = "jvs 快速开发平台";
    /**
     * 整个项目的密码key配置，默认使用jvs 如果需要自定义，需要前端配置修改配置
     */
    @Getter
    protected static String key = "jvs";
    /**
     * {@value env} 直接获取系统的命名默认获取规则为 spring.profiles.active
     * 方式，由于此配置默认使用@profiles.active@ ,所以会直接使用打包的时候的环境,或由项目启动时指定
     * 目前已经设置有dev|sit|uat|beta|pro五个环境，实际情况根据项目来
     */
    @Getter
    protected static String env = "";
    /**
     * 判断是开发,还是正式,如果是开发模式,某些功能将不走缓存,其它模式将公共资源,自动添加到本地缓存中,加快操作
     * 为开发模式,是true
     * 生成模式为false
     */
    @Getter
    protected static Boolean mode = false;

    @Getter
    private static String serverPort;

    @Getter
    protected static String random = "";
    /**
     * 添加mybatis_plus二级缓存前缀
     */
    @Getter
    protected static String mybatis_cache_prefix = "";
    /**
     * {@value version} 直接获取系统的命名默认获取规则为 project.version
     * 方式，由于此配置默认使用@project.version@ ,所以会直接使用pom中的项版本号
     * 业务系统版本号
     */
    @Getter
    protected static String version = "";
    protected static LocalDateTime dateTime;

    /**
     * 公共工具，可直调用此方法直接获取任何Spring管理的Bean对象，可以获取Mapper Service Component Configuration等
     *
     * @param var Bean的Class
     * @author: guojing
     * @return: T 返回实体的Bean对象，并可直接调用其方法
     */
    public static <T> T getBean(Class<T> var) {
        return applicationContext.getBean(var);
    }

    public static LocalDateTime getDate() {
        return dateTime;
    }

    public static String msg(String message, Object... o) {
        if (ObjectNull.isNotNull(applicationContext)) {
            try {
                String localMessage = Optional.of(getBean(MessageSource.class))
                        .map(e -> e.getMessage(message, o, message, LocaleContextHolder.getLocale()))
                        .orElse(StrUtil.format(message, o));
                if (Objects.isNull(o)) {
                    return localMessage;
                }
                if (message.equals(localMessage)) {
                    return StrUtil.format(message, o);
                } else {
                    return localMessage;
                }
            } catch (Exception e) {
                return StrUtil.format(message, o);
            }
        }
        return StrUtil.format(message, o);
    }

    /**
     * 重写Bean，主要为了初始化公共 {@linkplain ApplicationContext} 和初始化环境和名称对象
     *
     * @param context {@linkplain ApplicationContext}
     * @return: void
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        JSON.config(JSONWriter.Feature.WriteEnumsUsingName, JSONWriter.Feature.WriteNulls);
        dateTime = LocalDateTime.now();
        applicationContext = context;
        applicationContextName = context.getEnvironment().getProperty("spring.application.name");
        mybatis_cache_prefix = context.getEnvironment().getProperty("jvs.mybatis_cache_prefix", "auth");
        platformName = context.getEnvironment().getProperty("jvs.platform.name", "jvs 快速开发平台");
        key = context.getEnvironment().getProperty("gateway.encryptKey", "jvs");
        JvsAppSecretUtils.setSecretKey(context.getEnvironment().getProperty("design.secretKey"));
        serverPort = String.valueOf(context.getBean(ServerProperties.class).getPort());
        String namespace = context.getEnvironment().getProperty("spring.cloud.nacos.discovery.namespace");
        String envformat = "[%s]-[%s]-[%s]-[%s]";
        env = String.format(envformat, mode, applicationContextName, serverPort, namespace);
    }

    /**
     * 默认初始化{@linkplain RestTemplate}
     *
     * @return org.springframework.web.client.RestTemplate
     */
    @Bean
    @Primary
    @LoadBalanced
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate lbRestTemplate() {
        // 获取上下文配置的ClientHttpRequestInterceptor 实现
        Map<String, ClientHttpRequestInterceptor> beansOfType = applicationContext
                .getBeansOfType(ClientHttpRequestInterceptor.class);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(new ArrayList<>(beansOfType.values()));
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
        return restTemplate;
    }

    /**
     * 添加元数据，自定义元数据
     */
    @Autowired
    public Void metaData(Environment environment, ServiceInstance instance) {
        Map<String, String> metadata = instance.getMetadata();

        metadata.put("create_time", DateUtil.now());
        version = metadata.getOrDefault(SysConstant.VERSION, environment.getProperty("version"));
        String name = environment.getProperty("swagger.title");
        if (ObjectNull.isNotNull(name)) {
            metadata.put("name", name);
        }

        try {
            String property = System.getProperty("os.name");
            Properties properties = System.getProperties();
            String s = properties.get("java.io.tmpdir").toString();
            String file = s.replaceAll("AppData\\\\Local\\\\Temp\\\\", "") + ".gitconfig";
            String mac = "Mac";
            if (property.contains(mac)) {
                file = "~/.gitconfig";
            }
            Optional<String> any = FileUtil.readUtf8Lines(file).stream().filter(e -> e.contains("=")).findAny();
            if (any.isPresent()) {
                String[] split = any.get().trim().split("=");
                version = split[1].trim();
            }
        } catch (Exception ignored) {
        }
        metadata.put(SysConstant.VERSION, version);
        random = UUID.randomUUID().toString();
        metadata.put("random", random);
        return null;
    }

    /**
     * 判断当前服务是否在线
     *
     * @return
     */
    public static boolean thisServerStats() {
        NacosDiscoveryProperties bean = getBean(NacosDiscoveryProperties.class);
        String ip = bean.getIp();
        return SpringContextUtil.getBean(NacosDiscoveryClient.class)
                .getInstances(SpringContextUtil.getApplicationContextName()).stream()
                .anyMatch(e -> e.getHost().equals(ip) && e.getPort() == bean.getPort());
    }

    /**
     * 启用多语言
     *
     * @return
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "i18n/messages");
        messageSource.setDefaultEncoding(Charset.defaultCharset().name());
        messageSource.setCacheSeconds(3600 * 24);
        return messageSource;
    }

}
