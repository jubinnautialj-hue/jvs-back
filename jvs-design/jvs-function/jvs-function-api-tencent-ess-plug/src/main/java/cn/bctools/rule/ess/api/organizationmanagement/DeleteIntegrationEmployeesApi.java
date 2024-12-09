package cn.bctools.rule.ess.api.organizationmanagement;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DeleteIntegrationEmployeesRequest;
import com.tencentcloudapi.ess.v20201111.models.DeleteIntegrationEmployeesResponse;
import com.tencentcloudapi.ess.v20201111.models.Staff;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Delete integration employees api.
 *
 * @author jvs
 */
public class DeleteIntegrationEmployeesApi {

    /**
     * Delete integration employees delete integration employees response.
     *
     * @param operatorId the operator id
     * @param employees  the employees
     * @return the delete integration employees response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static DeleteIntegrationEmployeesResponse deleteIntegrationEmployees(String operatorId, Staff[] employees) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        DeleteIntegrationEmployeesRequest request = new DeleteIntegrationEmployeesRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setEmployees(employees);

        return client.DeleteIntegrationEmployees(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            Staff employee = new Staff();
            employee.setUserId("************");

            DeleteIntegrationEmployeesResponse response = DeleteIntegrationEmployeesApi.
                    deleteIntegrationEmployees(Config.OPERATOR_USER_ID, new Staff[]{employee});


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
