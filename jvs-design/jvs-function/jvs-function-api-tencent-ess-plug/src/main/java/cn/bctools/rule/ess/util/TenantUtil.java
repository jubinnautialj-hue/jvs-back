package cn.bctools.rule.ess.util;

import cn.bctools.common.utils.PasswordUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.ess.TencenCloudApiOption;
import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.common.TmtClientUtil;
import com.alibaba.fastjson2.JSONObject;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.tmt.v20180321.TmtClient;

/**
 * @author jvs
 * The type Tenant util.
 */
public class TenantUtil {
    /**
     * Gets client.
     *
     * @param option the option
     * @return the client
     */
    public static EssClient getClient(String option) {
        TencenCloudApiOption object = JSONObject.parseObject(PasswordUtil.decodedPassword(option, SpringContextUtil.getKey()), TencenCloudApiOption.class);
        return Client.getEssClient(object.getSecretId(), object.getSecretKey(), object.getEndPoint());
    }

    /**
     * Gets tmt client.
     *
     * @param option the option
     * @return the tmt client
     */
    public static TmtClient getTmtClient(String option) {
        TencenCloudApiOption object = JSONObject.parseObject(PasswordUtil.decodedPassword(option, SpringContextUtil.getKey()), TencenCloudApiOption.class);
        return TmtClientUtil.getTmtClient(object.getSecretId(), object.getSecretKey(), object.getEndPoint());
    }
}
