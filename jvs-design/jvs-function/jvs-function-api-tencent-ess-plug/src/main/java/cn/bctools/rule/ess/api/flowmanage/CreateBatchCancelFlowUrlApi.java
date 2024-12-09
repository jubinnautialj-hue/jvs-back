package cn.bctools.rule.ess.api.flowmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.*;

/**
 * @author jvs
 */
public class CreateBatchCancelFlowUrlApi {

    public static CreateBatchCancelFlowUrlResponse createBatchCancelFlowUrl(String operatorId, String[] flowIds) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreateBatchCancelFlowUrlRequest request = new CreateBatchCancelFlowUrlRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowIds(flowIds);

        return client.CreateBatchCancelFlowUrl(request);
    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        try {
            String[] flowIds = new String[]{"****************", "****************"};

            CreateBatchCancelFlowUrlResponse response = CreateBatchCancelFlowUrlApi.createBatchCancelFlowUrl(Config.OPERATOR_USER_ID, flowIds);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


