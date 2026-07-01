package cn.bctools.rule.ess.api.certificatemanagement;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowEvidenceReportRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowEvidenceReportResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Describe flow evidence report api.
 *
 * @author jvs
 */
public class DescribeFlowEvidenceReportApi {

    /**
     * Describe flow evidence report describe flow evidence report response.
     *
     * @param operatorId the operator id
     * @param reportId   the report id
     * @return the describe flow evidence report response
     * @throws Exception the exception
     */
    public static DescribeFlowEvidenceReportResponse describeFlowEvidenceReport(String operatorId, String reportId)
            throws Exception {

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        DescribeFlowEvidenceReportRequest request = new DescribeFlowEvidenceReportRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setReportId(reportId);

        return client.DescribeFlowEvidenceReport(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            // 出证报告编号
            String reportId = "****************";

            DescribeFlowEvidenceReportResponse response = DescribeFlowEvidenceReportApi.describeFlowEvidenceReport(Config.OPERATOR_USER_ID, reportId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

