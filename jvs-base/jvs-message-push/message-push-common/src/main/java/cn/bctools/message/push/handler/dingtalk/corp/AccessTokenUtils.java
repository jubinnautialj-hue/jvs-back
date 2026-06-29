package cn.bctools.message.push.handler.dingtalk.corp;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;

/**
 * 钉钉token获取工具
 * @author guojing
 */
public class AccessTokenUtils {

    public static TimedCache<String, String> accessTokenTimedCache;
    static final String ACCESS_TOKEN = "accessToken";

    private static TimedCache<String, String> getAccessTokenTimedCache(String appKey, String appSecret) throws ApiException {

        if (accessTokenTimedCache == null || StringUtils.isEmpty(accessTokenTimedCache.get(ACCESS_TOKEN))) {
            synchronized (AccessTokenUtils.class) {
                if (accessTokenTimedCache == null || StringUtils.isEmpty(accessTokenTimedCache.get(ACCESS_TOKEN))) {
                    DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
                    OapiGettokenRequest request = new OapiGettokenRequest();
                    request.setAppkey(appKey);
                    request.setAppsecret(appSecret);
                    request.setHttpMethod("GET");
                    OapiGettokenResponse response;
                    response = client.execute(request);
                    accessTokenTimedCache = CacheUtil.newTimedCache((response.getExpiresIn() - 60) * 1000);
                    accessTokenTimedCache.put(ACCESS_TOKEN, response.getAccessToken());
                }
            }
        }
        return accessTokenTimedCache;
    }

    public static String getAccessToken(String appKey, String appSecret) throws ApiException {
        return getAccessTokenTimedCache(appKey, appSecret).get(ACCESS_TOKEN);
    }

}
