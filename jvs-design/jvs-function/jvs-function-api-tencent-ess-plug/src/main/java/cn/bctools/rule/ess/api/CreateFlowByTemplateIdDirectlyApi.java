package cn.bctools.rule.ess.api;

import cn.bctools.rule.ess.api.flowmanage.CreateDocumentApi;
import cn.bctools.rule.ess.api.flowmanage.CreateFlowApi;
import cn.bctools.rule.ess.api.flowmanage.CreateSchemeUrlApi;
import cn.bctools.rule.ess.api.flowmanage.StartFlowApi;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.FlowCreateApprover;
import lombok.experimental.UtilityClass;

/**
 * 通过模板发起签署流程，并查询签署链接
 *
 * @author jvs
 */
@UtilityClass
public class CreateFlowByTemplateIdDirectlyApi {
    /**
     * Create flow by template id directly string [ ].
     *
     * @param operatorId the operator id
     * @param templateId the template id
     * @param flowName   the flow name
     * @param approvers  the approvers
     * @param isAutoSign the is auto sign
     * @return the string [ ]
     * @throws Exception the exception
     */
    public static String[] createFlowByTemplateIdDirectly(String operatorId, String templateId,
                                                          String flowName, FlowCreateApprover[] approvers,
                                                          boolean isAutoSign) throws Exception {
        // 1、创建流程
        String flowId = CreateFlowApi.createFlow(operatorId, flowName, approvers, isAutoSign);

        // 2、创建电子文档
        // --- 注意 formFields 与模板保持一样  CreateDocument.packFormFieldsExample()
        CreateDocumentApi.createDocument(operatorId, flowId, templateId, flowName,
                CreateDocumentApi.packFormFieldsExample());

        // 3、等待文档异步合成
        Thread.sleep(3000);

        // 4、开启流程
        StartFlowApi.startFlow(operatorId, flowId);

        // 5、获取签署链接
        CreateSchemeUrlResponse response = CreateSchemeUrlApi.createSchemeUrl(operatorId, flowId);

        return new String[]{flowId, response.getSchemeUrl()};
    }
}
