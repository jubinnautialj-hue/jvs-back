package cn.bctools.screen.filter;

import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 公共拦截器 用于处理整个系统锁定
 *
 * @author xiaohui
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommonInterceptor implements HandlerInterceptor {

    static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断前缀是否为 /no/auth 如果是 直接清空租户
        String requestUrl = request.getRequestURI();
        //判断是否需要拦截
        boolean match = ANT_PATH_MATCHER.match("/share/link/no/auth/**", requestUrl);
        if (match) {
            TenantContextHolder.clear();
        }
        return true;
    }
}
