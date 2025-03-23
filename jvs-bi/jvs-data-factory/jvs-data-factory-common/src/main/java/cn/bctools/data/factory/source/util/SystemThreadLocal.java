package cn.bctools.data.factory.source.util;


import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * @author admin
 * @describe 统一数据保存
 */
public class SystemThreadLocal {

    private static ThreadLocal<Map<String, Object>> local = new TransmittableThreadLocal<Map<String, Object>>();

    public static void set(String k, Object v) {
        Map<String, Object> map = local.get();
        if (map == null) {
            map = new HashMap<>(80);
            local.set(map);
        }
        local.get().put(k, v);
    }

    public static <T> T get(String key) {
        Map<String, Object> stringObjectMap = local.get();
        if (stringObjectMap == null) {
            HashMap<String, Object> map = new HashMap<>(80);
            local.set(map);
        }
        return (T) local.get().get(key);
    }

    public static void remove(String key) {
        local.get().remove(key);
    }

    public static void removeAll() {
        local.remove();
    }
}
