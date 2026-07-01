package cn.bctools.rule.ess.impl.certificatemanagementrelatedinterfaces;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleExceptionEnum;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.ess.util.TenantUtil;
import cn.bctools.rule.exception.RuleException;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import com.tencentcloudapi.ess.v20201111.models.VerifyPdfRequest;
import com.tencentcloudapi.ess.v20201111.models.VerifyPdfResponse;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "流程文件验签",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "对合同流程文件进行数字签名验证，判断数字签名是否有效，合同文件内容是否被篡改。"
)
@AllArgsConstructor
public class ProcessDocumentVerificationServiceImpl implements BaseCustomFunctionInterface<ProcessDocumentVerificationDto> {

    @Override
    public Object execute(ProcessDocumentVerificationDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            VerifyPdfRequest req = new VerifyPdfRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowId(dto.getFlowId());
            // 返回的resp是一个VerifyPdfResponse的实例，与请求对象对应

            VerifyPdfResponse resp = client.VerifyPdf(req);
            return BeanCopyUtil.copy(resp, HashMap.class);

        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
