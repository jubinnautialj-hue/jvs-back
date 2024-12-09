package cn.bctools.rule.ess.byfile;

import cn.bctools.rule.ess.api.fileuploaddownload.DescribeFileUrlsApi;
import cn.bctools.rule.ess.api.CreateFlowByFileDirectlyApi;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.ess.v20201111.models.ApproverInfo;
import com.tencentcloudapi.ess.v20201111.models.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jvs
 * 使用文件发起合同QuickStart
 */
@Slf4j

public class ByFileQuickStart {

    /**
     * ByFileQuickStart
     */
    public static void main(String[] args) throws Exception {

        // Step 1
        // 定义文件所在的路径
        String inputFilePath = "/Users/guojing/IdeaProjects/jvs-design/jvs-function/jvs-function-api-open-platform/src/main/java/cn/bctools/design/blank.pdf";
        // 定义合同名
        String flowName = "我的第一个合同";
        String operatorId = Config.OPERATOR_USER_ID;

        /// 构造签署人
        /// 此块代码中的$approvers仅用于快速发起一份合同样例，非正式对接用
        // 个人签署方的姓名，必须是真实的才能正常签署
        String personApproverName = "xxx";
        // 个人签署方的手机号，必须是真实的才能正常签署
        String personApproverMobile = "xxxxx";
        ApproverInfo[] approvers = buildPersonApprover(personApproverName, personApproverMobile);

        // 如果是正式接入，请参考入BuildApprovers函数内查看说明，构造需要的场景参数
        // approvers = BuildApprovers();

        // Step 2
        // 使用文件发起合同
        // 发起合同
        String[] resp = CreateFlowByFileDirectlyApi.createFlowByFileDirectly(operatorId, flowName, approvers, inputFilePath);
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
     * 打包个人签署方参与者信息 ApproverType 1：个人
     *
     * @param name
     * @param mobile
     * @return
     */
    private static ApproverInfo[] buildPersonApprover(String name, String mobile) {

        // 签署参与者信息
        // 个人签署方
        ApproverInfo approverInfo = new ApproverInfo();
        approverInfo.setApproverType(1L);
        approverInfo.setApproverName(name);
        approverInfo.setApproverMobile(mobile);
        approverInfo.setNotifyType("sms");
        // 签署人对应的签署控件
        Component component = buildComponent("SIGN_SIGNATURE", "", 417.15625F, 497.671875F, 74F, 70F, 0L, 1L);

        approverInfo.setSignComponents(new Component[]{component});

        return new ApproverInfo[]{approverInfo};
    }

    /**
     * 打包企业签署方参与者信息
     *
     * @param organizationName
     * @param name
     * @param mobile
     * @return
     */
    private static ApproverInfo[] buildOrganizationApprover(String organizationName, String name, String mobile) {

        // 签署参与者信息
        // 企业签署方
        ApproverInfo organizationApprover = new ApproverInfo();
        organizationApprover.setApproverType(0L);
        organizationApprover.setOrganizationName(organizationName);
        organizationApprover.setApproverName(name);
        organizationApprover.setApproverMobile(mobile);
        organizationApprover.setNotifyType("none");

        Component component = buildComponent("SIGN_SEAL", "", 120F, 120F, 74F, 70F, 0L, 1L);

        organizationApprover.setSignComponents(new Component[]{component});
        return new ApproverInfo[]{organizationApprover};
    }

    /**
     * 打包企业静默签署方参与者信息
     *
     * @param serverSignSealId
     * @return
     */
    private static ApproverInfo[] buildServerSignApprover(String serverSignSealId) {
        // 签署参与者信息
        // 企业签署方
        ApproverInfo serverSignApprover = new ApproverInfo();
        serverSignApprover.setApproverType(3L);

        Component component = buildComponent("SIGN_SEAL", serverSignSealId, 200F, 200F, 74F, 70F, 0L, 1L);

        serverSignApprover.setSignComponents(new Component[]{component});
        return new ApproverInfo[]{serverSignApprover};
    }

    /**
     * 构造签署人 - 以B2B2C为例, 实际请根据自己的场景构造签署方、控件
     *
     * @return
     */
    private static ApproverInfo[] buildApprovers() {
        // 发起方企业静默签署，此处需要在Config.java中设置一个持有的印章值 serverSignSealId
        ApproverInfo[] serverSignApprover = buildServerSignApprover(Config.SERVER_SIGN_SEAL_ID);

        // 另一家企业签署方
        String organizationName = "****************";
        String organizationUserName = "****************";
        String organizationUserMobile = "****************";
        ApproverInfo[] organizationApprover = buildOrganizationApprover(organizationName,
                organizationUserName, organizationUserMobile);

        // 个人签署方
        String personApproverName = "****************";
        String personApproverMobile = "****************";
        ApproverInfo[] personApprover = buildPersonApprover(personApproverName, personApproverMobile);

        return concatApproverInfos(serverSignApprover, organizationApprover, personApprover);
    }

    /**
     * 把多个签署者列表合并为一个签署者列表
     *
     * @param arrays
     * @return
     */
    private static ApproverInfo[] concatApproverInfos(ApproverInfo[]... arrays) {
        int length = 0;
        for (ApproverInfo[] array : arrays) {
            length += array.length;
        }
        ApproverInfo[] result = new ApproverInfo[length];
        int pos = 0;
        for (ApproverInfo[] array : arrays) {
            for (ApproverInfo element : array) {
                result[pos] = element;
                pos++;
            }
        }
        return result;
    }

    /**
     * 构建控件信息
     */
    public static Component buildComponent(String componentType, String componentValue,
                                           float componentPosX, float componentPosY, float componentWidth, float componentHeight, long fileIndex, long componentPage) {
        // 模板控件信息
        // 签署人对应的签署控件
        Component component = new Component();

        component.setComponentType(componentType);
        component.setComponentValue(componentValue);
        component.setComponentPosX(componentPosX);
        component.setComponentPosY(componentPosY);
        component.setComponentWidth(componentWidth);
        component.setComponentHeight(componentHeight);
        component.setFileIndex(fileIndex);
        component.setComponentPage(componentPage);

        return component;
    }
}
