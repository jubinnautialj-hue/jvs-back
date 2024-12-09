package cn.bctools.rule.ess.api.flowmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CancelFlowRequest;
import com.tencentcloudapi.ess.v20201111.models.CancelFlowResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * @author jvs
 */
public class CancelFlowApi {

    public static CancelFlowResponse cancelFlow(String operatorId, String flowId, String cancelMessage)
            throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CancelFlowRequest request = new CancelFlowRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowId(flowId);
        request.setCancelMessage(cancelMessage);

        // 如果接口报错，会抛出TencentCloudSDKException，不会输出response，请捕获TencentCloudSDKException，参考main函数

        return client.CancelFlow(request);

    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        try {
            String flowId = "****************";

            String cancelMessage = "撤销原因";

            CancelFlowResponse response = CancelFlowApi.cancelFlow(Config.OPERATOR_USER_ID, flowId, cancelMessage);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
