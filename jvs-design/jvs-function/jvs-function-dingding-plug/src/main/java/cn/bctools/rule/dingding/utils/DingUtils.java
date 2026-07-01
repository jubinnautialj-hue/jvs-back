package cn.bctools.rule.dingding.utils;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigDing;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import com.aliyun.dingtalktodo_1_0.Client;
import com.aliyun.dingtalktodo_1_0.models.CreateTodoTaskHeaders;
import com.aliyun.teaopenapi.models.Config;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

/**
 * The type Ding utils.
 *
 * @author jvs
 */
@Slf4j
public class DingUtils {

    /**
     * Gets create
     *
     * @param tenantId the tenant id
     * @return the create
     */
    public static CreateTodoTaskHeaders getCreateTodoTaskHeaders(String tenantId) {
        String accessToken = getAccessToken(tenantId);
        CreateTodoTaskHeaders header = new CreateTodoTaskHeaders();
        header.setXAcsDingtalkAccessToken(accessToken);
        return header;
    }

    /**
     * Gets create
     *
     * @return the create 
     */
    public static CreateTodoTaskHeaders getCreateTodoTaskHeaders() {
        return getCreateTodoTaskHeaders(TenantContextHolder.getTenantId());
    }


    /**
     * Gets access token.
     *
     * @param tenantId the tenant id
     * @return the access token
     */
    public static String getAccessToken(String tenantId) {
        SysConfigDing config = getSysConfigDing(tenantId);
        return getAccessToken(config);
    }

    /**
     * Gets sys config ding.
     *
     * @param tenantId the tenant id
     * @return the sys config ding
     */
    public static SysConfigDing getSysConfigDing(String tenantId) {
        AuthTenantConfigServiceApi api = SpringContextUtil.getBean(AuthTenantConfigServiceApi.class);
        String data = api.key(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION, tenantId).getData();
        SysConfigDing config = api.convertKey(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION, data);
        return config;
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    @SneakyThrows
    public static Client getClient() {
        return getClient(TenantContextHolder.getTenantId());
    }

    /**
     * Gets client.
     *
     * @param tenantId the tenant id
     * @return the client
     */
    public static Client getClient(String tenantId) {
        SysConfigDing config = getSysConfigDing(tenantId);
        return getClient(config);
    }

    /**
     * Get client client.
     *
     * @param config the config
     * @return the client
     */
    @SneakyThrows
    public static Client getClient(SysConfigDing config) {
        Config config2 = new Config();
        config2.setAccessKeyId(config.getAppId());
        config2.setAccessKeySecret(config.getAppSecret());
        config2.setRegionId("central");
        config2.setProtocol("https");
        Client client = new Client(config2);
        return client;
    }

    /**
     * 获取AccessToken
     *
     * @param config the config
     * @return access token
     */
    @SneakyThrows
    public static String getAccessToken(SysConfigDing config) {
        if (config.getEnable()) {
            // 缓存中无accessToken，发起请求获取accessToken
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
            OapiGettokenRequest req = new OapiGettokenRequest();
            req.setAppkey(config.getAppId());
            req.setAppsecret(config.getAppSecret());
            req.setHttpMethod(HttpMethod.GET.name());
            OapiGettokenResponse rsp = client.execute(req);
            if (0 != rsp.getErrcode().intValue()) {
                log.error("钉钉免登失败。获取access_token失败：{}", rsp.getBody());
                throw new BusinessException("system_abnormality");
            }
            String token = rsp.getAccessToken();
            if (StringUtils.isBlank(token)) {
                log.error("登录失败，获取accessToken失败");
                throw new BusinessException("system_abnormality");
            }
            return token;
        } else {
            throw new BusinessException("system_abnormality");
        }
    }
}
