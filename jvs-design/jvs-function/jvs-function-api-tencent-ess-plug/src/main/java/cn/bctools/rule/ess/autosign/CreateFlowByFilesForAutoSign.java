package cn.bctools.rule.ess.autosign;

import cn.bctools.rule.ess.api.fileuploaddownload.DescribeFileUrlsApi;
import cn.bctools.rule.ess.api.fileuploaddownload.UploadFilesApi;
import cn.bctools.rule.ess.api.flowmanage.CreateSchemeUrlApi;
import cn.bctools.rule.ess.common.Client;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.*;
import lombok.extern.slf4j.Slf4j;


/**
 * 使用文件发起合同QuickStart 医疗自动签专用 B2CC
 * <p>
 * 关键字定位签署坐标（请根据实际PDF中的文件调整关键字）
 *
 * @author jvs
 */
@Slf4j

public class CreateFlowByFilesForAutoSign {

    /**
     * CreateFlowByFilesForAutoSign
     *
     * @param args the input arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {

        // Step 1

        String inputFilePath = "src/main/resources/medical.pdf";

        String flowName = "自动签署测试合同-" + System.currentTimeMillis();
        String operatorId = Config.OPERATOR_USER_ID;

        /// 构造自动签签署人
        /// 此块代码中的$approvers仅用于快速发起一份合同样例，非正式对接用
        // 医生的信息
        // 个人签署方的姓名，必须是真实的才能正常签署
        String personApproverName = "xx";
        // 个人签署方的手机号，必须是真实的才能正常签署
        String personApproverMobile = "xx";
        // 身份证号
        String idCardNumber = "xx";
        ApproverInfo approversForDoctor = buildAutoSignPersonApprover(personApproverName, personApproverMobile,
                idCardNumber, "医生", 20F, -30F, 100F, 100L);
        // 药师的信息
        // 个人签署方的姓名，必须是真实的才能正常签署
        String personApproverNameForMedical = "xx";
        // 个人签署方的手机号，必须是真实的才能正常签署
        String personApproverMobileForMedical = "xx";
        // 身份证号
        String idCardNumberForMedical = "xx";
        ApproverInfo approversForMedical = buildAutoSignPersonApprover(personApproverNameForMedical,
                personApproverMobileForMedical, idCardNumberForMedical,
                "药师", 20F, -30F, 100F, 100L);

        // 平台企业信息
        ApproverInfo approverForPlatform = buildServerSignApprover("中药处方",
                20F, -30F, 100F, 100L);

        ApproverInfo[] allApprovers = new ApproverInfo[]{approverForPlatform, approversForDoctor, approversForMedical};

        // Step 2
        // 使用文件发起合同
        // 发起合同
        String[] resp = autoSignCreateFlowByFileDirectly(operatorId, flowName, allApprovers, inputFilePath);
        log.info("您创建的合同id为: ");
        log.info(resp[0]);
        // 返回签署的链接
        log.info("签署链接（请在手机浏览器中打开）为：");
        log.info(resp[1]);

        // Step 3
        // 下载合同
        String url = DescribeFileUrlsApi.describeFileUrls(operatorId, resp[0]);
        // 返回合同下载链接
        log.info("请访问以下地址下载您的合同：");
        log.info(url);

    }

    /**
     * Auto sign create flow by file directly string [ ].
     *
     * @param operatorId    the operator id
     * @param flowName      the flow name
     * @param approverInfos the approver infos
     * @param filePath      the file path
     * @return the string [ ]
     * @throws Exception the exception
     */
    public static String[] autoSignCreateFlowByFileDirectly(String operatorId, String flowName,
                                                            ApproverInfo[] approverInfos,
                                                            String filePath) throws Exception {
        // 1、上传文件获取fileId
        String fileId = UploadFilesApi.uploadFile(operatorId, filePath);

        // 2、创建签署流程
        String flowId = autoSignCreateFlowByFiles(operatorId, flowName, approverInfos, fileId);

        // 3、获取签署链接
        CreateSchemeUrlResponse response = CreateSchemeUrlApi.createSchemeUrl(operatorId, flowId);

        return new String[]{flowId, response.getSchemeUrl()};
    }

    /**
     * Auto sign create flow by files string.
     *
     * @param operatorId the operator id
     * @param flowName   the flow name
     * @param approvers  the approvers
     * @param fileId     the file id
     * @return the string
     * @throws TencentCloudSDKException the tencent cloud sdk exception
     */
    public static String autoSignCreateFlowByFiles(String operatorId, String flowName,
                                                   ApproverInfo[] approvers, String fileId)
            throws TencentCloudSDKException {

        // 构造默认的api客户端调用实例
        EssClient client = Client.getEssClient();

        // 构造请求体
        CreateFlowByFilesRequest request = new CreateFlowByFilesRequest();

        // 调用方用户信息，参考通用结构
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(operatorId);
        request.setOperator(userInfo);

        request.setFlowName(flowName);
        request.setApprovers(approvers);

        request.setFileIds(new String[]{fileId});
        request.setAutoSignScene("E_PRESCRIPTION_AUTO_SIGN");
        request.setUnordered(true);
        CreateFlowByFilesResponse response = client.CreateFlowByFiles(request);

        return response.getFlowId();
    }


    /**
     * 个人自动签署方参与者信息 ApproverType 1：个人 ApproverType 7：个人自动签署，需要开通自动签后使用
     *
     * @param name
     * @param mobile
     * @param idCardNum
     * @param keyWord
     * @param offSetX
     * @param offsetY
     * @param width
     * @param height
     * @return
     */
    private static ApproverInfo buildAutoSignPersonApprover(String name, String mobile, String idCardNum,
                                                            String keyWord, float offSetX, float offsetY,
                                                            float width, float height) {
        // 签署参与者信息
        // 个人签署方
        ApproverInfo approverInfo = new ApproverInfo();
        approverInfo.setApproverType(7L);
        approverInfo.setApproverName(name);
        approverInfo.setApproverMobile(mobile);
        approverInfo.setApproverIdCardType("ID_CARD");
        approverInfo.setApproverIdCardNumber(idCardNum);

        Component component = buildKeyWordComponent(keyWord, "Right", offSetX, offsetY, width, height, "", "");

        approverInfo.setSignComponents(new Component[]{component});
        return approverInfo;
    }

    /**
     * Build key word component component.
     *
     * @param componentId      the component id
     * @param relativeLocation the relative location
     * @param offSetX          the off set x
     * @param offSetY          the off set y
     * @param componentWidth   the component width
     * @param componentHeight  the component height
     * @param componentType    the component type
     * @param componentValue   the component value
     * @return the component
     */
    public static Component buildKeyWordComponent(String componentId, String relativeLocation,
                                                  float offSetX, float offSetY, float componentWidth,
                                                  float componentHeight, String componentType, String componentValue) {
        // 模板控件信息
        // 签署人对应的签署控件
        Component component = new Component();

        // ComponentId 关键字
        component.setComponentId(componentId);

        component.setComponentType("SIGN_SIGNATURE");

        component.setComponentWidth(componentWidth);

        component.setComponentHeight(componentHeight);

        component.setFileIndex(0L);

        component.setComponentPage(1L);

        component.setGenerateMode("KEYWORD");

        component.setOffsetX(offSetX);

        component.setOffsetY(offSetY);

        component.setRelativeLocation(relativeLocation);
        if (!componentType.isEmpty()) {
            component.setComponentType(componentType);
        }

        if (!componentValue.isEmpty()) {
            component.setComponentValue(componentValue);
        }

        return component;
    }


    private static ApproverInfo buildServerSignApprover(String keyWord, float offSetX, float offsetY,
                                                        float width, float height) {
        ApproverInfo approverInfo = new ApproverInfo();
        approverInfo.setApproverType(3L);
        Component component = buildKeyWordComponent(keyWord, "Right", offSetX, offsetY,
                width, height, "SIGN_SEAL", Config.SERVER_SIGN_SEAL_ID);
        approverInfo.setSignComponents(new Component[]{component});
        return approverInfo;
    }
}
