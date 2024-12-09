package cn.bctools.rule.ess.api.flowmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.*;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Create flow sign review api.
 *
 * @author jvs
 */
@Slf4j

public class CreateFlowSignReviewApi {

    /**
     * Create flow sign review create flow sign review response.
     *
     * @param operatorId    the operator id
     * @param flowId        the flow id
     * @param reviewType    the review type
     * @param reviewMessage the review message
     * @return the create flow sign review response
     * @throws Exception the exception
     */
    public static CreateFlowSignReviewResponse createFlowSignReview(String operatorId, String flowId, String reviewType,
                                                                    String reviewMessage) throws Exception {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreateFlowSignReviewRequest request = new CreateFlowSignReviewRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowId(flowId);
        request.setReviewType(reviewType);
        request.setReviewMessage(reviewMessage);

        CreateFlowSignReviewResponse resp = client.CreateFlowSignReview(request);
        return resp;
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            String flowId = "****************";

            String reviewType = "****************";

            String reviewMessage = "****************";

            CreateFlowSignReviewResponse resp = CreateFlowSignReviewApi.
                    createFlowSignReview(Config.OPERATOR_USER_ID, flowId, reviewType, reviewMessage);
            log.info("requestId: " + resp.getRequestId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

