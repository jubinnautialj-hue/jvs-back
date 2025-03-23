package cn.bctools.rule.ess.impl.processsigning;//package cn.bctools.rule.ess.impl.processsigning;
//
//import cn.bctools.common.exception.BusinessException;
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.ess.util.TenantUtil;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import com.tencentcloudapi.common.exception.TencentCloudSDKException;
//import com.tencentcloudapi.ess.v20201111.EssClient;
//import com.tencentcloudapi.ess.v20201111.models.CreateFlowRemindsRequest;
//import lombok.AllArgsConstructor;
//
//import java.util.Map;
//
///**
// * @author gl
// */
//@Rule(value = "模板发起合同-创建签署流程",
//        group = RuleGroup.腾讯电子签,
//        test = true,
//        enable = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 2,
////        iconUrl = "rule-dysmsduanxinfuwu",
//        explain = "==="
//)
//@AllArgsConstructor
//public class TemplateInitiateContractCreateSigningProcessServiceImpl implements BaseCustomFunctionInterface<CreateTemplateSigningProcessDto> {
//
//    @Override
//    public Object execute(CreateTemplateSigningProcessDto dto, Map<String, Object> params) {
//        EssClient client = TenantUtil.getClient(dto.getOptions());
//        // 实例化一个请求对象,每个接口都会对应一个request对象
//        CreateFlowRemindsRequest req = new CreateFlowRemindsRequest();
//        try {
//                        return BeanCopyUtil.copy(resp, HashMap.class);
//        } catch (TencentCloudSDKException e) {
//            log.error(e);
//                        throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
//        }
//
//    }
//}
