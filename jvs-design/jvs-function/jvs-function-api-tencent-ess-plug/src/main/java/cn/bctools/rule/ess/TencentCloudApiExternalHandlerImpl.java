//package cn.bctools.rule.ess;
//
//import cn.bctools.common.exception.BusinessException;
//import cn.bctools.rule.ess.api.CreateFlowByTemplateIdDirectlyApi;
//import cn.bctools.rule.ess.api.fileuploaddownload.DescribeFileUrlsApi;
//import cn.bctools.rule.ess.common.Client;
//import cn.bctools.rule.ess.config.Config;
//import cn.bctools.rule.function.RuleExternalHandler;
//import cn.hutool.http.Method;
//import com.tencentcloudapi.ess.v20201111.EssClient;
//import com.tencentcloudapi.ess.v20201111.models.FlowCreateApprover;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
///**
// * @author wl
// */
//@Slf4j
//@Service("腾讯电子签")
//@AllArgsConstructor
//public class TencentCloudApiExternalHandlerImpl implements RuleExternalHandler<TencenCloudApiOption> {
//
//    @Override
//    public Object handler(String url, Method method, Map<String, Object> hashMap, TencenCloudApiOption object, Map<String, String> headerMap) {
//        EssClient essClient = Client.getEssClient(object.getSecretId(), object.getSecretKey(), object.getEndPoint());
//        essClient.CreateFlowGroupByTemplates()
//        throw new BusinessException("目前不支持");
//    }
//
//
//    /**
//     * ByFileQuickStart
//     */
//    public static void main(String[] args) throws Exception {
//        // Step 1
//        // 定义合同名
//        String flowName = "我的第一个合同";
//        String operatorId = Config.OperatorUserId;
//        String templateId = Config.TemplateId;
//
//        /// 构造签署人
//        /// 此块代码中的$approvers仅用于快速发起一份合同样例
//        String personApproverName = "****************"; // 个人签署方的姓名，必须是真实的才能正常签署
//        String personApproverMobile = "****************"; // 个人签署方的手机号，必须是真实的才能正常签署
//        FlowCreateApprover[] approvers = BuildPersonFlowCreateApprover(personApproverName, personApproverMobile);
//
//        // 如果是正式接入，请参考入buildApprovers函数内查看说明，构造需要的场景参数
//        // approvers = buildApprovers();
//
//        // Step 2
//        // 使用文件发起合同
//        // 发起合同
//        String[] resp = CreateFlowByTemplateIdDirectlyApi.CreateFlowByTemplateIdDirectly(operatorId, templateId, flowName, approvers, false);
//        log.info("您创建的合同id为: ");
//        log.info(resp[0]);
//        // 返回签署的链接
//        log.info("签署链接（请在手机浏览器中打开）为：");
//        log.info(resp[1]);
//
//        // Step 3
//        // 下载合同
//        String url = DescribeFileUrlsApi.DescribeFileUrls(operatorId, resp[0]);
//        // 返回合同下载链接
//        log.info("请访问以下地址下载您的合同：");
//        log.info(url);
//    }
//
//    // 打包个人签署方参与者信息 ApproverType 1：个人
//    private static FlowCreateApprover[] BuildPersonFlowCreateApprover(String name, String mobile) {
//
//        // 签署参与者信息
//        // 个人签署方
//        FlowCreateApprover approverInfo = new FlowCreateApprover();
//        approverInfo.setApproverType(1L);
//        approverInfo.setApproverName(name);
//        approverInfo.setApproverMobile(mobile);
//        approverInfo.setNotifyType("sms");
//
//        return new FlowCreateApprover[]{approverInfo};
//    }
//
//    // 打包企业签署方参与者信息
//    private static FlowCreateApprover[] BuildOrganizationFlowCreateApprover(String organizationName, String name, String mobile) {
//
//        // 签署参与者信息
//        // 企业签署方
//        FlowCreateApprover organizationApprover = new FlowCreateApprover();
//        organizationApprover.setApproverType(0L);
//        organizationApprover.setOrganizationName(organizationName);
//        organizationApprover.setApproverName(name);
//        organizationApprover.setApproverMobile(mobile);
//        organizationApprover.setNotifyType("none");
//
//        return new FlowCreateApprover[]{organizationApprover};
//    }
//
//    // 打包企业静默签署方参与者信息
//    private static FlowCreateApprover[] BuildServerSignFlowCreateApprover(String serverSignSealId) {
//        // 签署参与者信息
//        // 企业签署方
//        FlowCreateApprover serverSignApprover = new FlowCreateApprover();
//
//        serverSignApprover.setApproverType(3L);
//
//        return new FlowCreateApprover[]{serverSignApprover};
//    }
//
//    // 构造签署人 - 以B2B2C为例, 实际请根据自己的场景构造签署方、控件
//    private static FlowCreateApprover[] BuildFlowCreateApprovers() {
//        // 发起方企业静默签署，此处需要在Config.java中设置一个持有的印章值 serverSignSealId
//        FlowCreateApprover[] serverSignApprover = BuildServerSignFlowCreateApprover(Config.ServerSignSealId);
//
//        // 另一家企业签署方
//        String organizationName = "****************";
//        String organizationUserName = "****************";
//        String organizationUserMobile = "****************";
//        FlowCreateApprover[] organizationApprover = BuildOrganizationFlowCreateApprover(organizationName,
//                organizationUserName, organizationUserMobile);
//
//        // 个人签署方
//        String personApproverName = "****************";
//        String personApproverMobile = "****************";
//        FlowCreateApprover[] personApprover = BuildPersonFlowCreateApprover(personApproverName, personApproverMobile);
//
//        return ConcatApproverInfos(serverSignApprover, organizationApprover, personApprover);
//    }
//
//    // 把多个签署者列表合并为一个签署者列表
//    private static FlowCreateApprover[] ConcatApproverInfos(FlowCreateApprover[]... arrays) {
//        int length = 0;
//        for (FlowCreateApprover[] array : arrays) {
//            length += array.length;
//        }
//        FlowCreateApprover[] result = new FlowCreateApprover[length];
//        int pos = 0;
//        for (FlowCreateApprover[] array : arrays) {
//            for (FlowCreateApprover element : array) {
//                result[pos] = element;
//                pos++;
//            }
//        }
//        return result;
//    }
//}
