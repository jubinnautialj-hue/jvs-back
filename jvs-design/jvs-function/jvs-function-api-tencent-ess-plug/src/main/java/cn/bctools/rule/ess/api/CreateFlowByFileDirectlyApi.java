package cn.bctools.rule.ess.api;

import cn.bctools.rule.ess.api.fileuploaddownload.UploadFilesApi;
import cn.bctools.rule.ess.api.flowmanage.CreateFlowByFilesApi;
import cn.bctools.rule.ess.api.flowmanage.CreateSchemeUrlApi;
import com.tencentcloudapi.ess.v20201111.models.ApproverInfo;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlResponse;

/**
 * The type Create flow by file directly api.
 *
 * @author jvs
 */
public class CreateFlowByFileDirectlyApi {

    /**
     * Create flow by file directly string [ ].
     *
     * @param operatorId    the operator id
     * @param flowName      the flow name
     * @param approverInfos the approver infos
     * @param filePath      the file path
     * @return the string [ ]
     * @throws Exception the exception
     */
    public static String[] createFlowByFileDirectly(String operatorId, String flowName, ApproverInfo[] approverInfos,
                                                    String filePath) throws Exception {
        // 1、上传文件获取fileId
        String fileId = UploadFilesApi.uploadFile(operatorId, filePath);

        // 2、创建签署流程
        String flowId = CreateFlowByFilesApi.createFlowByFiles(operatorId, flowName, approverInfos, fileId);

        // 3、获取签署链接
        CreateSchemeUrlResponse response = CreateSchemeUrlApi.createSchemeUrl(operatorId, flowId);

        return new String[]{flowId, response.getSchemeUrl()};
    }
}
