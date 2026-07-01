package cn.bctools.rule.ess.api.documentmanagement;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CancelMultiFlowSignQRCodeRequest;
import com.tencentcloudapi.ess.v20201111.models.CancelMultiFlowSignQRCodeResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * @author jvs
 */
public class CancelMultiFlowSignQRCodeApi {

    public static CancelMultiFlowSignQRCodeResponse cancelMultiFlowSignQrCode(String operatorId, String qrCodeId)
            throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CancelMultiFlowSignQRCodeRequest request = new CancelMultiFlowSignQRCodeRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setQrCodeId(qrCodeId);

        return client.CancelMultiFlowSignQRCode(request);
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        try {
            String qrCodeId = "****************";

            CancelMultiFlowSignQRCodeResponse response = CancelMultiFlowSignQRCodeApi.cancelMultiFlowSignQrCode(Config.OPERATOR_USER_ID, qrCodeId);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
