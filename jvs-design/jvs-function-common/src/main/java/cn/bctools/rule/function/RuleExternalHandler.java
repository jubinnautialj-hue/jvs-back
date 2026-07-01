package cn.bctools.rule.function;

import cn.hutool.http.Method;

import java.util.Map;

/**
 * @author wl
 */
public interface RuleExternalHandler<T> {
    /**
     * 自定义业务实现
     *
     * @param url       地址
     * @param method    方法类型
     * @param hashMap   请求的对象参数
     * @param account   配置选择的帐号信息
     * @param headerMap 请求头信息
     * @return
     */
    Object handler(String url, Method method, Map<String, Object> hashMap, T account, Map<String, String> headerMap);

}
