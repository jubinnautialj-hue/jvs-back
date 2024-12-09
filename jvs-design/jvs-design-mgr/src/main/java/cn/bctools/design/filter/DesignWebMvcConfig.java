package cn.bctools.design.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Data
@Configuration
public class DesignWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    BaseInterceptor baseInterceptor;
    @Autowired
    AppInterceptor baseAppInterceptor;

    /**
     * 忽略权限校验
     */
    private static final String IGNORE_APP_PERMISSION_VERIFICATION_PATTERN = "/base/**";
    /**
     * 三方系统或前端调用逻辑引擎的时候处理的方式
     */
    private static final String RULE_API_VERIFICATION_PATTERN = "/rule/openapi/**";
    /**
     * 默认放行的地址前缀
     */
    private static final List<String> DEFAULT_PERMIT_URLS = new ArrayList<>();

    static {
        String[] urlPatterns = {
                IGNORE_APP_PERMISSION_VERIFICATION_PATTERN,
                RULE_API_VERIFICATION_PATTERN,
                "/sys/home/**",
                "/api/**",
                "/frpc",
                "/fileLabel",
                "/file/**",
                "/register/**",
                // 大屏后续改
                "/screen/template/**",
                "/tree/list",
                // 表示是平台的， 也是需要校验是否是平台级管理员
                "/platform/**",
                "/webjars/**",
                "/resources/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/v3/**",
                "/doc.html",
                "index.html",
                "/jvsFunction/**",
        };
        DEFAULT_PERMIT_URLS.addAll(Arrays.asList(urlPatterns));
    }

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(baseInterceptor).addPathPatterns("/**");
        // 模式操作拦截器
        registry.addInterceptor(new ModeOperateInterceptor())
                .addPathPatterns("/app/design/**");
        // 应用权限拦截器,拦截所有请求并单独处理每一个设计的权限，是否是具有权限
        registry.addInterceptor(baseAppInterceptor)
                //放开地址
                .excludePathPatterns(DEFAULT_PERMIT_URLS)
                .pathMatcher(PATH_MATCHER)
                //其它全拦截
                .addPathPatterns("/**");
    }
}
