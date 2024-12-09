package cn.bctools.rule.ess.api.flowmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreatePrepareFlowRequest;
import com.tencentcloudapi.ess.v20201111.models.CreatePrepareFlowResponse;
import com.tencentcloudapi.ess.v20201111.models.FlowCreateApprover;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Create prepare flow api.
 *
 * @author jvs
 */
@Slf4j

public class CreatePrepareFlowApi {

    /**
     * Create prepare flow create prepare flow response.
     *
     * @param operatorId the operator id
     * @param flowName   the flow name
     * @param resourceId the resource id
     * @param approvers  the approvers
     * @return the create prepare flow response
     * @throws Exception the exception
     */
    public static CreatePrepareFlowResponse createPrepareFlow(String operatorId, String flowName, String resourceId, FlowCreateApprover[] approvers)
            throws Exception {

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreatePrepareFlowRequest request = new CreatePrepareFlowRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowName(flowName);

        request.setApprovers(approvers);

        request.setResourceId(resourceId);

        return client.CreatePrepareFlow(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            String flowName = "****************";
            String resourceId = "y************0s";

            FlowCreateApprover personInfo = new FlowCreateApprover();
            personInfo.setApproverType(1L);
            personInfo.setApproverName("李四");
            personInfo.setApproverMobile("1*********4");

            FlowCreateApprover[] approvers = new FlowCreateApprover[]{personInfo};

            CreatePrepareFlowResponse resp = CreatePrepareFlowApi.createPrepareFlow(Config.OPERATOR_USER_ID, flowName, resourceId, approvers);
            log.info("url: " + resp.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
