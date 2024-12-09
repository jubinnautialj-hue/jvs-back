package cn.bctools.rule.ess.api.documentmanagement;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.VerifyPdfRequest;
import com.tencentcloudapi.ess.v20201111.models.VerifyPdfResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Verify pdf api.
 *
 * @author jvs
 */
public class VerifyPdfApi {

    /**
     * Verify pdf verify pdf response.
     *
     * @param operatorId the operator id
     * @param flowId     the flow id
     * @return the verify pdf response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static VerifyPdfResponse verifyPdf(String operatorId, String flowId) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        VerifyPdfRequest request = new VerifyPdfRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowId(flowId);

        return client.VerifyPdf(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            String flowId = "****************";

            VerifyPdfResponse response = VerifyPdfApi.verifyPdf(Config.OPERATOR_USER_ID, flowId);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
