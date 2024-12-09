package cn.bctools.rule.ess.common;

import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.tmt.v20180321.TmtClient;


/**
 * 客户端调用实例
 *
 * @author jvs
 */
public class TmtClientUtil {

    /**
     * 构造默认Api客户端调用实例
     *
     * @return tmt client
     */
    public static TmtClient getTmtClient() {
        return getTmtClient(Config.SECRET_ID, Config.SECRET_KEY, Config.END_POINT);
    }

    /**
     * 构造默认File客户端调用实例
     *
     * @return ess file client
     */
    public static TmtClient getEssFileClient() {
        return getTmtClient(Config.SECRET_ID, Config.SECRET_KEY, Config.FILE_SERVICE_END_POINT);
    }

    /**
     * 构造Ess客户端调用实例
     *
     * @param secretId  腾讯云的密钥对secretId
     * @param secretKey 腾讯云的密钥对secretKey
     * @param endPoint  腾讯云的服务域名
     * @return Ess客户端调用实例 tmt client
     */
    public static TmtClient getTmtClient(String secretId, String secretKey, String endPoint) {
        // 实例化一个证书对象，入参需要传入腾讯云账户secretId，secretKey
        Credential credential = new Credential(secretId, secretKey);
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = getClientProfile(endPoint);
        String guangzhouRegion = "ap-guangzhou";
        TmtClient client = new TmtClient(credential, guangzhouRegion, clientProfile);
        return client;
    }

    /**
     * 构造Ess客户端配置
     *
     * @param endPoint 腾讯云的服务域名
     * @return Ess客户端配
     */
    private static ClientProfile getClientProfile(String endPoint) {
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = getHttpProfile(endPoint);
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        // 指定签名算法(默认为HmacSHA256)
        clientProfile.setSignMethod("TC3-HMAC-SHA256");
        clientProfile.setHttpProfile(httpProfile);

        return clientProfile;
    }

    /**
     * 构造http配置信息
     *
     * @param endPoint 腾讯云的服务域名
     * @return http配置信息
     */
    private static HttpProfile getHttpProfile(String endPoint) {
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        // 请求超时时间，单位为秒(默认60秒)
        httpProfile.setConnTimeout(30);
        // httpProfile.setReadTimeout(15);
        // httpProfile.setWriteTimeout(15);
        // post请求(默认为post请求)
        httpProfile.setReqMethod("POST");
        // 指定接入地域域名(默认就近接入)
        httpProfile.setEndpoint(endPoint);
        return httpProfile;
    }
}
