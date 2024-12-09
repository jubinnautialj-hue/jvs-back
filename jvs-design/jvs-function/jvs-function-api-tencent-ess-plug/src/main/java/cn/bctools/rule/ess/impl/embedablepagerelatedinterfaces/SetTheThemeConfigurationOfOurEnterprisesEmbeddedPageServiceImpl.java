//package cn.bctools.rule.ess.impl.embedablepagerelatedinterfaces;
//
//import cn.bctools.common.exception.BusinessException;
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.entity.enums.TestShowEnum;
//import cn.bctools.rule.ess.impl.processsigning.CreateTemplateSigningProcessDto;
//import cn.bctools.rule.ess.util.TenantUtil;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import com.tencentcloudapi.common.exception.TencentCloudSDKException;
//import com.tencentcloudapi.ess.v20201111.EssClient;
//import com.tencentcloudapi.ess.v20201111.models.CreateFlowRemindsRequest;
//import com.tencentcloudapi.ess.v20201111.models.CreateWebThemeConfigRequest;
//import com.tencentcloudapi.ess.v20201111.models.CreateWebThemeConfigResponse;
//import lombok.AllArgsConstructor;
//
//import java.util.Map;
//
///**
// * @author gl
// */
//@Rule(value = "设置本企业嵌入式页面主题配置",
//        group = RuleGroup.腾讯电子签,
//        test = true,
//        enable = true,
//        returnType = ClassType.对象,
//        testShowEnum = TestShowEnum.JSON,
//        order = 2,
////        iconUrl = "rule-dysmsduanxinfuwu",
//        explain = "用来设置本企业嵌入式页面个性化主题配置（例如是否展示电子签logo、定义主题色等），设置后获取的web签署界面都会使用此配置进行展示。\n" +
//                "\n" +
//                "如果多次调用，会以最后一次的配置为准"
//)
//@AllArgsConstructor
//public class SetTheThemeConfigurationOfOurEnterprisesEmbeddedPageServiceImpl implements BaseCustomFunctionInterface<SetTheThemeConfigurationOfOurEnterprisesEmbeddedPageDto> {
//
//    @Override
//    public Object execute(SetTheThemeConfigurationOfOurEnterprisesEmbeddedPageDto dto, Map<String, Object> params) {
//        EssClient client = TenantUtil.getClient(dto.getOptions());
//        // 实例化一个请求对象,每个接口都会对应一个request对象
//        try {
//            // 实例化一个请求对象,每个接口都会对应一个request对象
//            CreateWebThemeConfigRequest req = new CreateWebThemeConfigRequest();
//
//            // 返回的resp是一个CreateWebThemeConfigResponse的实例，与请求对象对应
//            CreateWebThemeConfigResponse resp = client.CreateWebThemeConfig(req);
//
//            return resp;
//        } catch (TencentCloudSDKException e) {
//            log.error(e);
//                        throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
//        }
//
//    }
//}
