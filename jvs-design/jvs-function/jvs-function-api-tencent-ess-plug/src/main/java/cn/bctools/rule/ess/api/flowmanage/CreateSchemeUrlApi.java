package cn.bctools.rule.ess.api.flowmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Create scheme url api.
 *
 * @author jvs
 */
public class CreateSchemeUrlApi {

    /**
     * Create scheme url create scheme url response.
     *
     * @param operatorId the operator id
     * @param flowId     the flow id
     * @return the create scheme url response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static CreateSchemeUrlResponse createSchemeUrl(String operatorId, String flowId) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreateSchemeUrlRequest request = new CreateSchemeUrlRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowId(flowId);

        request.setPathType(1L);

        return client.CreateSchemeUrl(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            String flowId = "****************";

            CreateSchemeUrlResponse response = CreateSchemeUrlApi.createSchemeUrl(Config.OPERATOR_USER_ID, flowId);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
