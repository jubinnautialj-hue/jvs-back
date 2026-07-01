package cn.bctools.rule.ess.api.flowmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.StartFlowRequest;
import com.tencentcloudapi.ess.v20201111.models.StartFlowResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * @author jvs
 * The type Start flow api.
 */
public class StartFlowApi {

    /**
     * Start flow start flow response.
     *
     * @param operatorId the operator id
     * @param flowId     the flow id
     * @return the start flow response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static StartFlowResponse startFlow(String operatorId, String flowId) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        StartFlowRequest request = new StartFlowRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        // 签署流程编号，由CreateFlow接口返回
        request.setFlowId(flowId);

        return client.StartFlow(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            // 签署流程编号，由CreateFlow接口返回
            String flowId = "****************";

            StartFlowResponse response = StartFlowApi.startFlow(Config.OPERATOR_USER_ID, flowId);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
