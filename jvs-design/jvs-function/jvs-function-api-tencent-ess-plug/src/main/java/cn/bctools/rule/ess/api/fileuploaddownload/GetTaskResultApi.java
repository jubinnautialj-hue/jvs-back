package cn.bctools.rule.ess.api.fileuploaddownload;


import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.GetTaskResultApiRequest;
import com.tencentcloudapi.ess.v20201111.models.GetTaskResultApiResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;

/**
 * The type Get task result api.
 * 接口的统一返回处理
 *
 * @author jvs
 */
public class GetTaskResultApi {

    /**
     * Get task result get task result api response.
     *
     * @param operatorId the operator id
     * @param taskId     the task id
     * @return the get task result api response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static GetTaskResultApiResponse getTaskResult(String operatorId, String taskId)
            throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        GetTaskResultApiRequest request = new GetTaskResultApiRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setTaskId(taskId);

        return client.GetTaskResultApi(request);

    }

    /**
     * 测试
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            // 任务Id，通过CreateConvertTaskApi得到
            String taskId = "****************";

            GetTaskResultApiResponse response = GetTaskResultApi.getTaskResult(Config.OPERATOR_USER_ID, taskId);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
