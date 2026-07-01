package cn.bctools.design.rule.handler;

import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.rule.selectoption.QiChaChaOption;
import cn.bctools.rule.function.RuleExternalHandler;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wl
 */
@Slf4j
@Service("企查查")
@AllArgsConstructor
public class QIChaChaExternalHandlerImpl implements RuleExternalHandler<QiChaChaOption> {

    static Map<String, String> CODE_MAP = new HashMap<>(8);

    @Override
    public Object handler(String url, Method method, Map<String, Object> hashMap, QiChaChaOption option, Map<String, String> headerMap) {
        String[] strings = randomAuthentHeader(option.getAppkey(), option.getSecretKey());
        //判断处理方式 ,是否在本地有处理逻辑,如果有选择处理逻辑,直接使用本地处理逻辑
        HttpRequest request = HttpUtil.createRequest(method, url.concat((url.indexOf("?") > 0 ? "&" : "?") + "key=").concat(option.secretKey))
                .headerMap(headerMap, true)
                .header("Token", strings[0])
                .header("Timespan", strings[1])
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        if (ObjectNull.isNotNull(hashMap)) {
            request.form(hashMap);
        }
        String body = request.execute().body();
        if (!JSONPath.contains(body, "status")) {
            return body;
        }
        String status = JvsJsonPath.read(body, "status").toString();
        if ("200".equals(status)) {
            return JSON.parseObject(body);
        } else {
            //根据状态码转换
            if (CODE_MAP.containsKey(status)) {
                return CODE_MAP.get(status);
            }
            return body;
        }
    }

    /**
     * 获取Auth Code
     * @param appkey
     * @param secretKey
     * @return
     */
    protected static final String[] randomAuthentHeader(String appkey, String secretKey) {
        String timeSpan = String.valueOf(System.currentTimeMillis() / 1000);
        String[] authentHeaders = new String[]{DigestUtils.md5Hex(appkey.concat(timeSpan).concat(secretKey)).toUpperCase(), timeSpan};
        return authentHeaders;
    }

    static {
        CODE_MAP.put("201", "【有效请求】查询无结果");
        CODE_MAP.put("202", "【有效请求】查询参数错误，请检查");
        CODE_MAP.put("205", "【有效请求】等待处理中");
        CODE_MAP.put("207", "【有效请求】请求数据的条目数超过上限(5000)");
        CODE_MAP.put("208", "【有效请求】此接口不支持此公司类型查询");
        CODE_MAP.put("209", "【有效请求】企业数量超过上限");
        CODE_MAP.put("213", "【有效请求】参数长度不能小于2");
        CODE_MAP.put("215", "【有效请求】不支持的查询关键字");
        CODE_MAP.put("218", "【有效请求】该企业暂不支持空壳扫描");
        CODE_MAP.put("219", "【有效请求】该企业暂不支持准入尽调");
        CODE_MAP.put("105", "【有效请求】接口已下线停用");
        CODE_MAP.put("110", "【有效请求】当前相同查询连续出错，请等2小时后重试");
        CODE_MAP.put("101", "【无效请求】当前的KEY无效或者还未生效中");
        CODE_MAP.put("102", "【无效请求】当前KEY已欠费");
        CODE_MAP.put("103", "【无效请求】当前KEY被暂停使用");
        CODE_MAP.put("104", "【无效请求】请求KEY异常，请联系管理员");
        CODE_MAP.put("106", "【无效请求】非法请求过多，请联系管理员");
        CODE_MAP.put("107", "【无效请求】被禁止的IP或者签名错误");
        CODE_MAP.put("108", "【无效请求】异常请求过多，请联系管理员");
        CODE_MAP.put("109", "【无效请求】请求超过每日系统限制");
        CODE_MAP.put("111", "【无效请求】接口权限未开通，请联系管理员");
        CODE_MAP.put("112", "【无效请求】您的账号剩余使用量已不足或已过期");
        CODE_MAP.put("113", "【无效请求】当前接口已被删除，请重新申请");
        CODE_MAP.put("114", "【无效请求】当前接口已被禁用，请联系管理员");
        CODE_MAP.put("115", "【无效请求】身份验证错误或者已过期");
        CODE_MAP.put("116", "【无效请求】请求超过每日调用总量限制");
        CODE_MAP.put("117", "【无效请求】当前不支持的请求参数调用量过多");
        CODE_MAP.put("118", "【无效请求】当前接口不支持此方式的调用");
        CODE_MAP.put("119", "【无效请求】您的账号出现异常，请联系管理员");
        CODE_MAP.put("120", "【无效请求】系统流量异常，请稍后再试");
        CODE_MAP.put("121", "【无效请求】数据不能出境");
        CODE_MAP.put("122", "【无效请求】请求超过系统并发限制");
        CODE_MAP.put("123", "【无效请求】您的请求已达未认证权益上限，请及时认证");
        CODE_MAP.put("199", "【无效请求】系统未知错误，请联系技术客服");
        CODE_MAP.put("203", "【无效请求】系统查询有异常，请联系技术人员");
        CODE_MAP.put("214", "【无效请求】您还未购买过该接口，请先购买");
        CODE_MAP.put("223", "【无效请求】查询参数模糊，无法获取结果");
        CODE_MAP.put("224", "【无效请求】查询参数无效 ");
    }

}
