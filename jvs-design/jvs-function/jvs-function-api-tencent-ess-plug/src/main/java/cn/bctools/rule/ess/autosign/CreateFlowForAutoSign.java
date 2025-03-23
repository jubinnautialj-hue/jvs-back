package cn.bctools.rule.ess.autosign;

import cn.bctools.rule.ess.api.CreateFlowByTemplateIdDirectlyApi;
import cn.bctools.rule.ess.api.fileuploaddownload.DescribeFileUrlsApi;
import cn.bctools.rule.ess.config.Config;
import com.tencentcloudapi.ess.v20201111.models.FlowCreateApprover;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Create flow for auto sign.
 *
 * @author jvs
 */
@Slf4j

public class CreateFlowForAutoSign {

    /**
     * 构造签署人 - 以B2CC为例, 企业（静默签）-医生（个人自动签）-药师（个人自动签），实际请根据自己的场景构造签署方、控件
     *
     * @return
     */
    private static FlowCreateApprover[] buildFlowCreateApprovers() {
        // 发起方企业静默签署，此处需要在Config.java中设置一个持有的印章值 serverSignSealId
        FlowCreateApprover serverSignApprover = buildServerSignFlowCreateApprover();

        // 个人签署方 医生 注意修改为真实的姓名、手机号、证件号
        FlowCreateApprover personApprover1 = buildAutoSignPersonFlowCreateApprover(
                "医生姓名", "医生手机号", "医生身份证号");
        // 个人签署方 药师 注意修改为真实的姓名、手机号、证件号
        FlowCreateApprover personApprover2 = buildAutoSignPersonFlowCreateApprover(
                "药师姓名", "药师手机号", "药师身份证号");

        return new FlowCreateApprover[]{serverSignApprover, personApprover1, personApprover2};
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {

        try {
            // Step 1
            // 定义合同名
            String flowName = "自建应用-医疗签署合同" + System.currentTimeMillis();
            String operatorId = Config.OPERATOR_USER_ID;
            String templateId = Config.TEMPLATE_ID;

            // 构造签署人 - 以B2CC为例, 企业（静默签）-医生（个人自动签）-药师（个人自动签），实际请根据自己的场景构造签署方、控件
            // 注意修改姓名、手机号、身份证号
            FlowCreateApprover[] approvers = buildFlowCreateApprovers();

            // Step 2
            // 发起合同 注意这里 isAutoSign为true，对应的 AutoSignScene为E_PRESCRIPTION_AUTO_SIGN
            String[] resp = CreateFlowByTemplateIdDirectlyApi.createFlowByTemplateIdDirectly(operatorId,
                    templateId, flowName, approvers, true);
            log.info("您创建的合同id为: ");
            log.info(resp[0]);

            // Step 3
            // 下载合同
            String url = DescribeFileUrlsApi.describeFileUrls(operatorId, resp[0]);
            // 返回合同下载链接
            log.info("请访问以下地址下载您的合同：");
            log.info(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打包个人签署方参与者信息 ApproverType 7：个人自动签
     *
     * @param name
     * @param mobile
     * @param idCardNumber
     * @return
     */
    private static FlowCreateApprover buildAutoSignPersonFlowCreateApprover(String name,
                                                                            String mobile,
                                                                            String idCardNumber) {
        // 签署参与者信息
        // 个人签署方
        FlowCreateApprover approverInfo = new FlowCreateApprover();

        approverInfo.setApproverType(7L);

        approverInfo.setApproverName(name);

        approverInfo.setApproverMobile(mobile);

        approverInfo.setApproverIdCardType("ID_CARD");
        approverInfo.setApproverIdCardNumber(idCardNumber);

        approverInfo.setNotifyType("NONE");

        return approverInfo;
    }

    /**
     * 打包企业静默签署方参与者信息
     *
     * @return
     */
    private static FlowCreateApprover buildServerSignFlowCreateApprover() {
        // 签署参与者信息
        // 企业签署方
        FlowCreateApprover serverSignApprover = new FlowCreateApprover();

        serverSignApprover.setApproverType(3L);

        return serverSignApprover;
    }
}
