package cn.bctools.rule.ess.api.sealmanage;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DescribeOrganizationSealsRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeOrganizationSealsResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Describe organization seals api.
 *
 * @author jvs
 */
public class DescribeOrganizationSealsApi {

    /**
     * Describe organization seals describe organization seals response.
     *
     * @param operatorId the operator id
     * @param limit      the limit
     * @return the describe organization seals response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static DescribeOrganizationSealsResponse describeOrganizationSeals(String operatorId, long limit) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        DescribeOrganizationSealsRequest request = new DescribeOrganizationSealsRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setLimit(limit);

        return client.DescribeOrganizationSeals(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            long limit = 10L;
            DescribeOrganizationSealsResponse response = DescribeOrganizationSealsApi.describeOrganizationSeals(Config.OPERATOR_USER_ID, limit);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
