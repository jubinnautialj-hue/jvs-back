package cn.bctools.rule.ess.api.personaccountmanagement;

import cn.bctools.rule.ess.common.Client;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DescribeThirdPartyAuthCodeRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeThirdPartyAuthCodeResponse;

/**
 * The type Describe third party auth code api.
 *
 * @author jvs
 */
public class DescribeThirdPartyAuthCodeApi {

    /**
     * Describe third party auth code describe third party auth code response.
     *
     * @param authCode the auth code
     * @return the describe third party auth code response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static DescribeThirdPartyAuthCodeResponse describeThirdPartyAuthCode(String authCode)
            throws TencentCloudSDKException {

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        DescribeThirdPartyAuthCodeRequest request = new DescribeThirdPartyAuthCodeRequest();
        request.setAuthCode(authCode);

        return client.DescribeThirdPartyAuthCode(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            String authCode = "****************";

            DescribeThirdPartyAuthCodeResponse response = DescribeThirdPartyAuthCodeApi.describeThirdPartyAuthCode(authCode);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
