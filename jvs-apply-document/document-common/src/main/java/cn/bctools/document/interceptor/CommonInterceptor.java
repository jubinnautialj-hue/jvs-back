package cn.bctools.document.interceptor;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.document.constant.Constant;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 公共拦截器 用于处理整个系统锁定
 *
 * @author xiaohui
 */
@Configuration
@Lazy(false)
@Slf4j
public class CommonInterceptor implements HandlerInterceptor {
    @Autowired
    RedisUtils redisUtils;
    static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object o = redisUtils.get(Constant.LOCK_COMMON_KEY);
        if (o != null) {
            throw new BusinessException(StrUtil.format("当前系统处于锁定状态，原因为:{}", o.toString()));
        }
        //判断前缀是否为 /no/auth 如果是 直接清空租户
        String requestUrl = request.getRequestURI();
        //判断是否需要拦截
        boolean match = ANT_PATH_MATCHER.match("/no/auth/**", requestUrl);
        if (match) {
            TenantContextHolder.clear();
        }
        return true;
    }
}
