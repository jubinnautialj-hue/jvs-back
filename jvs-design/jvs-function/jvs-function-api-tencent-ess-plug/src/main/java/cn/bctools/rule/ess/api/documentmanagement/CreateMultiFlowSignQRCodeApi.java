package cn.bctools.rule.ess.api.documentmanagement;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreateMultiFlowSignQRCodeRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateMultiFlowSignQRCodeResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Create multi flow sign qr code api.
 *
 * @author jvs
 */
public class CreateMultiFlowSignQRCodeApi {

    /**
     * Create multi flow sign qr code create multi flow sign qr code response.
     *
     * @param operatorId the operator id
     * @param templateId the template id
     * @param flowName   the flow name
     * @return the create multi flow sign qr code response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static CreateMultiFlowSignQRCodeResponse createMultiFlowSignQrCode(String operatorId, String templateId, String flowName)
            throws TencentCloudSDKException {

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreateMultiFlowSignQRCodeRequest request = new CreateMultiFlowSignQRCodeRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setTemplateId(templateId);
        request.setFlowName(flowName);

        return client.CreateMultiFlowSignQRCode(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            String flowName = "****************";

            CreateMultiFlowSignQRCodeResponse response = CreateMultiFlowSignQRCodeApi.createMultiFlowSignQrCode(Config.OPERATOR_USER_ID, Config.TEMPLATE_ID, flowName);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
