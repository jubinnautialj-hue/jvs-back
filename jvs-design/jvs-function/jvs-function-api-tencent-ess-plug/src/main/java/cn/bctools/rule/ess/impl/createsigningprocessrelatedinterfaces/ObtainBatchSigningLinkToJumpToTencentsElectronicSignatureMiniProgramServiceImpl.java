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
import com.tencentcloudapi.ess.v20201111.models.CreateBatchSignUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateBatchSignUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "获取跳转至腾讯电子签小程序的批量签署链接",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "通过此接口，创建小程序批量签署链接，个人/企业员工点击此链接即可跳转小程序进行批量签署。 请确保生成链接时候的身份信息和签署合同参与方的信息保持一致。\n" +
                "\n" +
                "注：\n" +
                "\n" +
                "参与人点击链接后需短信验证码才能查看合同内容。\n" +
                "企业用户批量签署，需要传OrganizationName（参与方所在企业名称）参数生成签署链接，请确保此企业已完成腾讯电子签企业认证。\n" +
                "个人批量签署，签名区仅支持手写签名。"
)
@AllArgsConstructor
public class ObtainBatchSigningLinkToJumpToTencentsElectronicSignatureMiniProgramServiceImpl implements BaseCustomFunctionInterface<ObtainBatchSigningLinkToJumpToTencentsElectronicSignatureMiniProgramDto> {

    @Override
    public Object execute(ObtainBatchSigningLinkToJumpToTencentsElectronicSignatureMiniProgramDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateBatchSignUrlRequest req = new CreateBatchSignUrlRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowIds(dto.getFlowIds().toArray(new String[0]));
            req.setJumpToDetail(dto.getJumpToDetail());
            req.setIdCardNumber(dto.getIdCardNumber());
            req.setMobile(dto.getMobile());
            req.setName(dto.getName());
            req.setIdCardType(dto.getIdCardType());
            req.setNotifyType(dto.getNotifyType());
            req.setOrganizationName(dto.getOrganizationName());

            // 返回的resp是一个CreateBatchSignUrlResponse的实例，与请求对象对应
            CreateBatchSignUrlResponse resp = client.CreateBatchSignUrl(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
