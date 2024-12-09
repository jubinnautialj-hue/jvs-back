package cn.bctools.rule.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author guojing
 */
public class UrlUtils {

    /**
     * 获取请求路径上的参数
     *
     * @return Map参数
     */
    public static Map<String, Object> getUrlParams() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            return Collections.emptyMap();
        }
        HttpServletRequest request = requestAttributes.getRequest();
        Map<String, String[]> parameterMap = request.getParameterMap();
        HashMap<String, Object> urlParams = new HashMap<>(parameterMap.size());
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String[] value = entry.getValue();
            if (Objects.isNull(value)) {
                continue;
            }
            if (value.length == 1) {
                urlParams.put(key, value[0]);
            } else {
                urlParams.put(key, Arrays.asList(value));
            }
        }
        return urlParams;
    }


}
