package cn.bctools.rule.ess.autosign;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DescribeUserAutoSignStatusRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeUserAutoSignStatusResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Describe user auto sign status api.
 *
 * @author jvs
 */
@Slf4j
public class DescribeUserAutoSignStatusApi {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {

        DescribeUserAutoSignStatusRequest req = prepareDescribeUserAutoSignStatusRequest();
        req.setUserInfo(CreateUserAutoSignEnableUrlApi.prepareUserThreeFactor("姓名", "身份证号"));

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        try {
            DescribeUserAutoSignStatusResponse res = client.DescribeUserAutoSignStatus(req);
            assert res != null;
            log.info(DescribeUserAutoSignStatusResponse.toJsonString(res));
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
    }

    /**
     * 构造请求基本参数
     *
     * @return the describe user auto sign status request
     */
    public static DescribeUserAutoSignStatusRequest prepareDescribeUserAutoSignStatusRequest() {

        DescribeUserAutoSignStatusRequest req = new DescribeUserAutoSignStatusRequest();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Config.OPERATOR_USER_ID);
        req.setOperator(userInfo);
        req.setSceneKey("E_PRESCRIPTION_AUTO_SIGN");
        return req;
    }
}
