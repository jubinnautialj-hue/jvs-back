package cn.bctools.rule.ess.api.flowmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowBriefsRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowBriefsResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Describe flow briefs api.
 *
 * @author jvs
 */
public class DescribeFlowBriefsApi {

    /**
     * Describe flow briefs describe flow briefs response.
     *
     * @param operatorId the operator id
     * @param flowId     the flow id
     * @return the describe flow briefs response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static DescribeFlowBriefsResponse describeFlowBriefs(String operatorId, String flowId) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        DescribeFlowBriefsRequest request = new DescribeFlowBriefsRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowIds(new String[]{flowId});

        return client.DescribeFlowBriefs(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            String flowId = "****************";

            DescribeFlowBriefsResponse response = DescribeFlowBriefsApi.describeFlowBriefs(Config.OPERATOR_USER_ID, flowId);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
