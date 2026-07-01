package cn.bctools.rule.ess.impl.createsigningprocessrelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.*;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "创建一码多扫流程签署二维码",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "适用场景: 签署人可通过扫描二维码补充签署信息进行实名签署。常用于提前不知道签署人的身份信息场景，例如：劳务工招工、大批量员工入职等场景。\n" +
                "\n" +
                "注意:\n" +
                "\n" +
                "本接口适用于发起方没有填写控件的 B2C或者单C模板, 若是B2C模板,还要满足以下任意一个条件\n" +
                "模板中配置的签署顺序是无序\n" +
                "B端企业的签署方式是静默签署\n" +
                "B端企业是非首位签署\n" +
                "通过一码多扫二维码发起的合同，合同涉及到的回调消息可参考文档合同发起及签署相关回调\n" +
                "用户通过签署二维码发起合同时，因企业额度不足导致失败 会触发签署二维码相关回调"
)
@AllArgsConstructor
public class CreateOneCodeMultiScanProcessToSignQRCodeServiceImpl implements BaseCustomFunctionInterface<CreateOneCodeMultiScanProcessToSignQRCodeDto> {

    @Override
    public Object execute(CreateOneCodeMultiScanProcessToSignQRCodeDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateMultiFlowSignQRCodeRequest req = new CreateMultiFlowSignQRCodeRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowName(dto.getFlowName());
            try {
                req.setApproverComponentLimitTypes(BeanCopyUtil.copys(dto.getApproverComponentLimitTypes(), ApproverComponentLimitType.class).toArray(new ApproverComponentLimitType[dto.getApproverComponentLimitTypes().size()]));
            } catch (Exception ignored) {

            }
            req.setFlowEffectiveDay(dto.getFlowEffectiveDay());
            req.setMaxFlowNum(dto.getMaxFlowNum());
            req.setQrEffectiveDay(dto.getQrEffectiveDay());
            try {
                req.setRestrictions(BeanCopyUtil.copys(dto.getRestrictions(), ApproverRestriction.class).toArray(new ApproverRestriction[dto.getRestrictions().size()]));
            } catch (Exception ignored) {

            }
            req.setTemplateId(dto.getTemplateId());
            req.setUserData(dto.getUserData());
            // 返回的resp是一个CreateMultiFlowSignQRCodeResponse的实例，与请求对象对应
            CreateMultiFlowSignQRCodeResponse resp = client.CreateMultiFlowSignQRCode(req);

            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
