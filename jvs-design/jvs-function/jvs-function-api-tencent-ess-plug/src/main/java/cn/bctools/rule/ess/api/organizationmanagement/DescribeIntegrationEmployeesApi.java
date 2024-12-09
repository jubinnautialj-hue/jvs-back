package cn.bctools.rule.ess.api.organizationmanagement;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DescribeIntegrationEmployeesRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeIntegrationEmployeesResponse;
import com.tencentcloudapi.ess.v20201111.models.Filter;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Describe integration employees api.
 *
 * @author jvs
 */
public class DescribeIntegrationEmployeesApi {

    /**
     * Describe integration employees describe integration employees response.
     *
     * @param operatorId the operator id
     * @param limit      the limit
     * @param offset     the offset
     * @param filters    the filters
     * @return the describe integration employees response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static DescribeIntegrationEmployeesResponse describeIntegrationEmployees(String operatorId, long limit, long offset, Filter[] filters) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        DescribeIntegrationEmployeesRequest request = new DescribeIntegrationEmployeesRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFilters(filters);

        request.setLimit(limit);

        request.setOffset(offset);

        return client.DescribeIntegrationEmployees(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            long limit = 20;
            long offset = 0;
            Filter filter = new Filter();
            filter.setKey("Status");
            filter.setValues(new String[]{"IsVerified"});

            DescribeIntegrationEmployeesResponse response = DescribeIntegrationEmployeesApi.
                    describeIntegrationEmployees(Config.OPERATOR_USER_ID, limit, offset, new Filter[]{filter});


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
