package cn.bctools.design.rule.handler;

import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.rule.selectoption.TianYanChaOption;
import cn.bctools.rule.function.RuleExternalHandler;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wl
 */
@Slf4j
@Service("天眼查")
@AllArgsConstructor
public class TianYanChaExternalHandlerImpl implements RuleExternalHandler<TianYanChaOption> {

    static Map<String, String> CODE_MAP = new HashMap<>(8);
    static final String errorCode = "error_code";

    @Override
    public Object handler(String url, Method method, Map<String, Object> hashMap, TianYanChaOption option, Map<String, String> headerMap) {
        //判断处理方式 ,是否在本地有处理逻辑,如果有选择处理逻辑,直接使用本地处理逻辑
        HttpRequest request = HttpUtil.createRequest(method, url)
                .headerMap(headerMap, true)
                .header("Authorization", option.getToken())
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        if (ObjectNull.isNotNull(hashMap)) {
            request.form(hashMap);
        }
        String body = request.execute().body();
        if (!JSONPath.contains(body, errorCode)) {
            return body;
        }
        Integer status = (Integer) JvsJsonPath.read(body, "error_code");
        if (0 == status) {
            return JSON.parseObject(body);
        } else {
            //根据状态码转换
            if (CODE_MAP.containsKey(status)) {
                return CODE_MAP.get(status);
            }
            return body;
        }
    }

    static {
        CODE_MAP.put("300000", "无数据");
        CODE_MAP.put("300001", "请求失败");
        CODE_MAP.put("300002", "账号失效");
        CODE_MAP.put("300003", "账号过期");
        CODE_MAP.put("300004", "访问频率过快");
        CODE_MAP.put("300005", "无权限访问此api");
        CODE_MAP.put("300006", "余额不足");
        CODE_MAP.put("300007", "剩余次数不足");
        CODE_MAP.put("300008", "缺少必要参数");
        CODE_MAP.put("300009", "账号信息有误");
        CODE_MAP.put("300010", "URL不存在");
        CODE_MAP.put("300011", "此IP无权限访问此api");
        CODE_MAP.put("300012", "报告生成中");
    }
}
