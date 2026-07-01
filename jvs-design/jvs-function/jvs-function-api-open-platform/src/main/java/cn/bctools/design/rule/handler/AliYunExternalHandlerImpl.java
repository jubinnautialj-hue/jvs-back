package cn.bctools.design.rule.handler;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.rule.selectoption.AliYunMarketOption;
import cn.bctools.rule.function.RuleExternalHandler;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author wl
 */
@Slf4j
@Service("阿里云市场")
@AllArgsConstructor
public class AliYunExternalHandlerImpl implements RuleExternalHandler<AliYunMarketOption> {

    @Override
    public Object handler(String url, Method method, Map<String, Object> hashMap, AliYunMarketOption datasource, Map<String, String> headerMap) {
        //判断处理方式 ,是否在本地有处理逻辑,如果有选择处理逻辑,直接使用本地处理逻辑
        //需要固定appCode码
        HttpRequest request = HttpUtil.createRequest(method, url)
                .headerMap(headerMap, true)
                .header("Authorization", "APPCODE " + datasource.getAppCode())
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        if (ObjectNull.isNotNull(hashMap)) {
            request.form(hashMap);
        }
        String body = request.execute().body();
        if (JSONUtil.isTypeJSONObject(body)) {
            return JSONObject.parseObject(body);
        }
        throw new BusinessException("HTTP网络请求返回结构不支持");
    }

}
