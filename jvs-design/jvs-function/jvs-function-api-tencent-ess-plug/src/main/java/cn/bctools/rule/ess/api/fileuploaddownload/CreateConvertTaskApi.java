package cn.bctools.rule.ess.api.fileuploaddownload;


import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreateConvertTaskApiRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateConvertTaskApiResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * @author jvs
 */
public class CreateConvertTaskApi {

    public static CreateConvertTaskApiResponse createConvertTask(String operatorId, String resourceId, String resourceType, String resourceName)
            throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreateConvertTaskApiRequest request = new CreateConvertTaskApiRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setResourceType(resourceType);
        request.setResourceName(resourceName);
        request.setResourceId(resourceId);

        return client.CreateConvertTaskApi(request);

    }

    /**
     * 测试
     */
    public static void main(String[] args) {
        try {
            String resourceId = "****************";

            String resourceType = "****************";

            String resourceName = "****************";

            CreateConvertTaskApiResponse response = CreateConvertTaskApi.createConvertTask(Config.OPERATOR_USER_ID, resourceId, resourceType, resourceName);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
