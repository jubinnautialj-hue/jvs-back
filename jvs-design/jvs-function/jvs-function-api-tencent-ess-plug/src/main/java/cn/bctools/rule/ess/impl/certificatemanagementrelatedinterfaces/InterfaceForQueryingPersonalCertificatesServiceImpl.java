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
import com.tencentcloudapi.ess.v20201111.models.DescribePersonCertificateRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribePersonCertificateResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询个人证书接口",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "此接口（DescribePersonCertificate）用于查询个人数字证书信息。\n" +
                "注：1.目前仅用于查询开通了医疗自动签署功能的个人数字证书。\n" +
                "2.调用此接口需要开通白名单，使用前请联系相关人员开通白名单。"
)
@AllArgsConstructor
public class InterfaceForQueryingPersonalCertificatesServiceImpl implements BaseCustomFunctionInterface<InterfaceForQueryingPersonalCertificatesDto> {

    @Override
    public Object execute(InterfaceForQueryingPersonalCertificatesDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribePersonCertificateRequest req = BeanCopyUtil.copy(dto, DescribePersonCertificateRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));

            // 返回的resp是一个DescribePersonCertificateResponse的实例，与请求对象对应
            DescribePersonCertificateResponse resp = client.DescribePersonCertificate(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
