package cn.bctools.rule.ess.group;

import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.*;

/**
 * 查询模板
 * <p>
 * 官网文档：https://cloud.tencent.com/document/product/1323/74803
 * <p>
 * 适用场景：当模板较多或模板中的控件较多时，可以通过查询模板接口更方便的获取自己主体下的模板列表，以及每个模板内的控件信息。
 * 该接口常用来配合“创建电子文档”接口作为前置的接口使用。
 *
 * @author jvs
 */
public class GroupDescribeFlowTemplates {
    /**
     * 查询模板
     *
     * @param operatorId          经办人id
     * @param proxyOrganizationId 代发起子企业的企业id
     * @param templateId          流程id
     * @return DescribeFlowTemplatesResponse describe flow templates response
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static DescribeFlowTemplatesResponse describeFlowTemplates(String operatorId, String proxyOrganizationId, String templateId) throws TencentCloudSDKException {
        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        DescribeFlowTemplatesRequest request = new DescribeFlowTemplatesRequest();

        // 设置集团子企业账号
        Agent agent = new Agent();
        agent.setProxyOrganizationId(proxyOrganizationId);
        request.setAgent(agent);

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        // 需要查询的流程ID列表
        Filter filter = new Filter();
        filter.setKey("template-id");
        filter.setValues(new String[]{templateId});
        request.setFilters(new Filter[]{filter});

        return client.DescribeFlowTemplates(request);
    }

    /**
     * 主企业代子企业查询模板信息样例
     * 注意：使用集团代发起功能，需要主企业和子企业均已加入集团，并且主企业OperatorUserId对应人员被赋予了对应操作权限
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            // 需要代发起的子企业的企业id
            String proxyOrganizationId = "*****************";
            // 子企业的模板id
            String templateId = "*****************";

            DescribeFlowTemplatesResponse response = describeFlowTemplates(Config.OPERATOR_USER_ID, proxyOrganizationId, templateId);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
