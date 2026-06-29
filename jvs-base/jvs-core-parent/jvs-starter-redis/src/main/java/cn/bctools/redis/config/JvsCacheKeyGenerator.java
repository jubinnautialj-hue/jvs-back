package cn.bctools.redis.config;

import cn.bctools.redis.annotation.CacheTenantSkip;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 缓存生成规则,根据对象方法,参数,进行生成
 * 建议直接使用参数生成,
 * 如果添加了注解{@link CacheTenantSkip} 则会跳过多租户配置,生成的key中不包含
 * 不建议将此注解添加到类上
 *
 * @author gj
 */
public class JvsCacheKeyGenerator implements KeyGenerator {

    /**
     * 如果有自定义key值 ,使用自定义Key值,如果没有,使用类名加方法名小写拼凑
     *
     * @param target
     * @param method
     * @param params
     * @return
     */
    @Override
    public Object generate(Object target, Method method, Object... params) {
        //判断是否跳过多租户配置key
        List arrayList = new ArrayList<>(Arrays.asList(params));
        if (params.length == 0) {
            //没有一个key的参数,使用类名和方法名返回数据
            arrayList.add(target.getClass().getName());
            arrayList.add(method.getName());
            return new SimpleKey(arrayList.toArray());
        }
        if (arrayList.size() == 1) {
            Object param = arrayList.get(0);
            if (param != null && !param.getClass().isArray()) {
                return param;
            }
        }
        return new SimpleKey(arrayList.toArray());
    }

}
