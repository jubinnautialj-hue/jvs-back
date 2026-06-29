package cn.bctools.gateway.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * @author guojing
 */
public class ServerHttpRequestUtils {
    static final String KEY = "x-forwarded-host";

    public static String getHost(ServerHttpRequest request) {
        //判断是否存在代理
        if (request.getHeaders().containsKey(KEY)) {
            return request.getHeaders().getFirst(KEY);
        }
        return request.getHeaders().getFirst("host");
    }


    private static final String IP_UTILS_FLAG = ",";
    private static final String UNKNOWN = "unknown";
    /**
     * 获取IP地址
     * <p>
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     *
     * @param request
     */
    public static String getIpAddr(HttpHeaders request) {
        String ip = null;
        try {
            //以下两个获取在k8s中，将真实的客户端IP，放到了x-Original-Forwarded-For。而将WAF的回源地址放到了 x-Forwarded-For了。
            ip = request.getFirst("X-Original-Forwarded-For");
            if (ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getFirst("X-Forwarded-For");
            }
            //获取nginx等代理的ip
            if (ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getFirst("x-forwarded-for");
            }
            if (ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getFirst("Proxy-Client-IP");
            }
            if (ip.isEmpty() || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getFirst("WL-Proxy-Client-IP");
            }
            if (ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getFirst("HTTP_CLIENT_IP");
            }
            if (ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getFirst("HTTP_X_FORWARDED_FOR");
            }
        } catch (Exception e) {
        }
        //使用代理，则获取第一个IP地址
        if (!ip.isEmpty() && ip.indexOf(IP_UTILS_FLAG) > 0) {
            ip = ip.substring(0, ip.indexOf(IP_UTILS_FLAG));
        }

        return ip;
    }

}
