package cn.bctools.rule.ess.autosign;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DisableUserAutoSignRequest;
import com.tencentcloudapi.ess.v20201111.models.DisableUserAutoSignResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Disable user auto sign api.
 *
 * @author jvs
 */
@Slf4j

public class DisableUserAutoSignApi {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        DisableUserAutoSignRequest req = prepareDisableUserAutoSignRequest();
        req.setUserInfo(CreateUserAutoSignEnableUrlApi.prepareUserThreeFactor("姓名", "身份证号"));

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        try {
            DisableUserAutoSignResponse res = client.DisableUserAutoSign(req);
            assert res != null;
            log.info(DisableUserAutoSignResponse.toJsonString(res));
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造请求基本参数
     *
     * @return the disable user auto sign request
     */
    public static DisableUserAutoSignRequest prepareDisableUserAutoSignRequest() {

        DisableUserAutoSignRequest req = new DisableUserAutoSignRequest();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Config.OPERATOR_USER_ID);
        req.setOperator(userInfo);
        req.setSceneKey("E_PRESCRIPTION_AUTO_SIGN");
        return req;
    }
}
