package cn.bctools.rule.ess.api.fileuploaddownload;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DescribeFileUrlsRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFileUrlsResponse;
import com.tencentcloudapi.ess.v20201111.models.FileUrl;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Describe file urls api.
 *
 * @author jvs
 */
@Slf4j

public class DescribeFileUrlsApi {

    /**
     * Describe file urls string.
     *
     * @param operatorId the operator id
     * @param flowId     the flow id
     * @return the string
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static String describeFileUrls(String operatorId, String flowId) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        DescribeFileUrlsRequest request = new DescribeFileUrlsRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setBusinessType("FLOW");
        request.setBusinessIds(new String[]{flowId});

        DescribeFileUrlsResponse response = client.DescribeFileUrls(request);

        FileUrl[] urls = response.getFileUrls();

        return urls[0].getUrl();
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            String flowId = "****************";

            String url = DescribeFileUrlsApi.describeFileUrls(Config.OPERATOR_USER_ID, flowId);

            log.info("urls: " + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
