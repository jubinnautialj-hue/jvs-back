package cn.bctools.rule.ess.api.templatemanagement;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowTemplatesRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowTemplatesResponse;
import com.tencentcloudapi.ess.v20201111.models.Filter;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Describe flow templates api.
 *
 * @author jvs
 */
public class DescribeFlowTemplatesApi {

    /**
     * Describe flow templates describe flow templates response.
     *
     * @param operatorId the operator id
     * @param templateId the template id
     * @return the describe flow templates response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static DescribeFlowTemplatesResponse describeFlowTemplates(String operatorId, String templateId) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        DescribeFlowTemplatesRequest request = new DescribeFlowTemplatesRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        Filter filter = new Filter();
        filter.setKey("template-id");
        filter.setValues(new String[]{templateId});
        request.setFilters(new Filter[]{filter});

        return client.DescribeFlowTemplates(request);
    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            DescribeFlowTemplatesResponse response = DescribeFlowTemplatesApi.describeFlowTemplates(Config.OPERATOR_USER_ID, Config.TEMPLATE_ID);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
