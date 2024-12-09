package cn.bctools.rule.ess.impl.embedablepagerelatedinterfaces;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.ess.util.TenantUtil;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.ess.v20201111.EssClient;
import com.tencentcloudapi.ess.v20201111.models.CreateOrganizationBatchSignUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateOrganizationBatchSignUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "获取企业签署合同web页面",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "使用此接口，您可以创建企业批量签署链接，员工只需点击链接即可跳转至控制台进行批量签署。\n" +
                "附注：\n" +
                "\n" +
                "员工必须在企业下完成实名认证，且需作为批量签署合同的签署方。\n" +
                "如有UserId，应以UserId为主要标识；如果没有UserId，则必须填写Name和Mobile信息。\n"
)
@AllArgsConstructor
public class ObtainTheWebPageForEnterpriseContractSigningServiceImpl implements BaseCustomFunctionInterface<ObtainTheWebPageForEnterpriseContractSigningDto> {

    @Override
    public Object execute(ObtainTheWebPageForEnterpriseContractSigningDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateOrganizationBatchSignUrlRequest req = new CreateOrganizationBatchSignUrlRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setName(dto.getName());
            req.setMobile(dto.getMobile());
            req.setFlowIds(dto.getFlowIds().toArray(new String[0]));
            req.setUserId(dto.getUserId());
            // 返回的resp是一个CreateOrganizationBatchSignUrlResponse的实例，与请求对象对应
            CreateOrganizationBatchSignUrlResponse resp = client.CreateOrganizationBatchSignUrl(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new BusinessException("执行错误", e.getMessage());
        }
    }
}
