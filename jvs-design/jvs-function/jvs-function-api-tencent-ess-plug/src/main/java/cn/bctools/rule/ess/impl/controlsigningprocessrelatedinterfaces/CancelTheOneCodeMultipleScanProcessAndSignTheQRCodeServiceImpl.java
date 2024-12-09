package cn.bctools.rule.ess.impl.controlsigningprocessrelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.CancelMultiFlowSignQRCodeRequest;
import com.tencentcloudapi.ess.v20201111.models.CancelMultiFlowSignQRCodeResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "取消一码多扫流程签署二维码",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（CancelMultiFlowSignQRCode）用于废除一码多扫流程签署二维码。 该接口所需的二维码ID，源自创建一码多扫流程签署二维码生成的。 如果该二维码尚处于有效期内，可通过本接口将其设置为失效状态。"
)
@AllArgsConstructor
public class CancelTheOneCodeMultipleScanProcessAndSignTheQRCodeServiceImpl implements BaseCustomFunctionInterface<CancelTheOneCodeMultipleScanProcessAndSignTheQRCodeDto> {

    @Override
    public Object execute(CancelTheOneCodeMultipleScanProcessAndSignTheQRCodeDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CancelMultiFlowSignQRCodeRequest req = new CancelMultiFlowSignQRCodeRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setQrCodeId(dto.getQrCodeId());
            // 返回的resp是一个CancelMultiFlowSignQRCodeResponse的实例，与请求对象对应
            CancelMultiFlowSignQRCodeResponse resp = client.CancelMultiFlowSignQRCode(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
