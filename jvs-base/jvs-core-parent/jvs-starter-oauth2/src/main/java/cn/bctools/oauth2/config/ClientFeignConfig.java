package cn.bctools.oauth2.config;

import cn.bctools.common.constant.SysConstant;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.hutool.core.util.ObjectUtil;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Administrator
 */
@Slf4j
public class ClientFeignConfig {

    @Bean
    @ConditionalOnMissingBean
    public RequestInterceptor clientRequestInterceptor() {
        return (requestTemplate) -> {
            requestTemplate.header(SysConstant.TENANTID, String.valueOf(SystemThreadLocal.get(SysConstant.TENANTID)));
            requestTemplate.header(SysConstant.VERSION, String.valueOf(SystemThreadLocal.get(SysConstant.VERSION)));
        };
    }

    @Bean
    public ClientHttpRequestInterceptor jvsClientHttpRequestInterceptor() {
        return (request, body, execution) -> {
            HttpRequestWrapper requestWrapper = new HttpRequestWrapper(request);
            try {
                String authorization = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization");
                if (ObjectUtil.isNotEmpty(authorization)) {
                    requestWrapper.getHeaders().add("Authorization", authorization);
                }
            } catch (Exception e) {
            }
            return execution.execute(requestWrapper, body);
        };
    }
}
