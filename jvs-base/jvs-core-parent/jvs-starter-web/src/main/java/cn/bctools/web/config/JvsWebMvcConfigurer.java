package cn.bctools.web.config;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.feign.config.SwaggerProperties;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 利用重写Spring boot的拦截器实现，添加自定义的拦截并请请求中的TraceId和环境进行推送到日志中便于日志查询的排查
 *
 * @author My_gj
 */
@Slf4j
@Data
@Configuration
@ConditionalOnMissingBean(JvsWebMvcConfigurer.class)
public class JvsWebMvcConfigurer implements WebMvcConfigurer, ClientHttpRequestInterceptor, ApplicationRunner {

    @Autowired
    Tracer tracer;
    @Autowired
    SwaggerProperties swaggerProperties;

    /**
     * 添加拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //指定拦截器类
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                RequestContextHolder.setRequestAttributes(RequestContextHolder.getRequestAttributes(), true);
                SystemThreadLocal.set(SysConstant.VERSION, request.getHeader(SysConstant.VERSION));
                //将租户放到当前线程中
                if (ObjectNull.isNull(TenantContextHolder.getTenantId())) {
                    TenantContextHolder.setTenantId(request.getHeader(SysConstant.TENANTID));
                }
                String tid = tracer.currentSpan().context().traceId();
                MDC.put("tid", tid);
                MDC.put("env", SpringContextUtil.getEnv());
                return true;
            }
        }).addPathPatterns("/**");
    }

    /**
     * 添加RestTemplate插件
     * <p>
     * 将上下文中的版本号, 租户id放入请求头中
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
        String version = SystemThreadLocal.get(SysConstant.VERSION);
        if (ObjectUtil.isNotEmpty(version)) {
            requestWrapper.getHeaders().add(SysConstant.VERSION, version);
        }
        String tenantId = SystemThreadLocal.get(SysConstant.TENANTID);
        if (ObjectUtil.isNotEmpty(tenantId)) {
            requestWrapper.getHeaders().add(SysConstant.TENANTID, tenantId);
        }
        // 执行请求
        log.info("Request: " + request.getMethod() + ":" + request.getURI() + " " + "\nHeaders: " + JSONObject.toJSONString(request.getHeaders()) + " " + request.getURI() +
                "\nBody: " + new String(body));
        return execution.execute(request, body);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new EnumConvertorFactory());
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String str = "[" + swaggerProperties.getTitle() + "] 完成启动";
        String s = "============================================================================================================================================";
        int length = s.length();
        String collect = Stream.iterate(0, v -> ++v)
                .limit((length - str.length()) / 2)
                .map(v -> "=")
                .collect(Collectors.joining());
        log.info(s);
        log.info(s);
        log.info(s);
        log.info(collect + str + collect + "");
        log.info(s);
        log.info(s);
        log.info(s);
    }
}
