package cn.bctools.web.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 网络请求相关工具类
 *
 * @Author: GuoZi
 */
@UtilityClass
public class WebUtils {
    static final FastJsonHttpMessageConverter FAST_JSON_HTTP_MESSAGE_CONVERTER = new FastJsonHttpMessageConverter();
    static final String KEY = "x-forwarded-host";

    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            throw new BusinessException("请求信息不存在");
        }
        return requestAttributes;
    }

    public static String getHost() {
        return getHost(getRequest());
    }
    public static String getHost(HttpServletRequest request) {
        //判断是否存在代理
        if (ObjectNull.isNotNull(request.getHeader(KEY))) {
            return request.getHeader(KEY).toString();
        }
        return request.getHeader("host").toString();
    }

    @SneakyThrows
    public static void write(Object r, HttpServletResponse response) {
        FAST_JSON_HTTP_MESSAGE_CONVERTER.write(r, MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
    }
}
