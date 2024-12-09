package cn.bctools.rule.ess.api.flowmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowApproversRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowApproversResponse;
import com.tencentcloudapi.ess.v20201111.models.FillApproverInfo;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Create flow approvers api.
 *
 * @author jvs
 */
public class CreateFlowApproversApi {

    /**
     * Create flow approvers create flow approvers response.
     *
     * @param operatorId the operator id
     * @param flowId     the flow id
     * @param approvers  the approvers
     * @return the create flow approvers response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static CreateFlowApproversResponse createFlowApprovers(String operatorId, String flowId, FillApproverInfo[] approvers) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreateFlowApproversRequest request = new CreateFlowApproversRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowId(flowId);

        request.setApprovers(approvers);

        return client.CreateFlowApprovers(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            // 签署流程编号
            String flowId = "*****************";

            // 补充签署人信息
            FillApproverInfo approverInfo = new FillApproverInfo();

            approverInfo.setApproverSource("WEWORKAPP");

            approverInfo.setRecipientId("****************");

            approverInfo.setCustomUserId("***************");
            FillApproverInfo[] approvers = new FillApproverInfo[]{approverInfo};

            CreateFlowApproversResponse response = CreateFlowApproversApi.createFlowApprovers(Config.OPERATOR_USER_ID, flowId, approvers);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
