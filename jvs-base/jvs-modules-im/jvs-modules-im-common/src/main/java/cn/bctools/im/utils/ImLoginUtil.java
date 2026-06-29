package cn.bctools.im.utils;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: im登录工具类
 */
public class ImLoginUtil {

    public static final String TOKEN = "token";
    public static final String VERSION = "version";

    private static ThreadLocal<Map<String, String>> threadLocal = new TransmittableThreadLocal<Map<String, String>>();

    public static void set(String token, String version) {
        Map<String, String> map = new HashMap<>();
        map.put(TOKEN, token);
        map.put(VERSION, version);
        threadLocal.set(map);
    }

    public static Map<String, String> get() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }
}
