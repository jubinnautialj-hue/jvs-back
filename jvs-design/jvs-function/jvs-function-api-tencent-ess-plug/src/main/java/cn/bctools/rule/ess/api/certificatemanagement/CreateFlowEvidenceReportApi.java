package cn.bctools.rule.ess.api.certificatemanagement;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowEvidenceReportRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowEvidenceReportResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Create flow evidence report api.
 *
 * @author jvs
 */
public class CreateFlowEvidenceReportApi {

    /**
     * Create flow evidence report create flow evidence report response.
     *
     * @param operatorId the operator id
     * @param flowId     the flow id
     * @return the create flow evidence report response
     * @throws Exception the exception
     */
    public static CreateFlowEvidenceReportResponse createFlowEvidenceReport(String operatorId, String flowId)
            throws Exception {

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreateFlowEvidenceReportRequest request = new CreateFlowEvidenceReportRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowId(flowId);

        return client.CreateFlowEvidenceReport(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            // 签署流程编号
            String flowId = "****************";

            CreateFlowEvidenceReportResponse response = CreateFlowEvidenceReportApi.createFlowEvidenceReport(Config.OPERATOR_USER_ID, flowId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

