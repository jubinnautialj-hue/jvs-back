package cn.bctools.data.factory.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author admin
 */
@Slf4j
public class SystemTool<T> {

    public T get(String key) {
        return SystemThreadLocal.get(key);
    }

    public void set(String key, T dataScopeList) {
        SystemThreadLocal.set(key, dataScopeList);
    }

    public void removeAll() {
        SystemThreadLocal.removeAll();
    }

    public void remove(String key) {
        SystemThreadLocal.remove(key);
    }

}
