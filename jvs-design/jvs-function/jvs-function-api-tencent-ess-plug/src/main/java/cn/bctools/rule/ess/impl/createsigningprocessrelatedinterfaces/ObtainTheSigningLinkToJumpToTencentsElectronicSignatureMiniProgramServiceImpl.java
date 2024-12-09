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
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateSchemeUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "获取跳转至腾讯电子签小程序的签署链接",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "获取跳转至腾讯电子签小程序的签署链接\n" +
                "\n" +
                "适用场景：如果需要签署人在自己的APP、小程序、H5应用中签署，可以通过此接口获取跳转腾讯电子签小程序的签署跳转链接。\n" +
                "\n" +
                "跳转到小程序的实现，参考微信官方文档（分为全屏、半屏两种方式），如何配置也可以请参考: 跳转电子签小程序配置\n" +
                "\n" +
                "注： 1. 如果签署人是在PC端扫码签署，可以通过生成跳转链接自主转换成二维码，让签署人在PC端扫码签署 2. 签署链接的有效期为90天，超过有效期链接不可用 3. 如果需跳转详情页（即PathType值为1）进行填写或签署合同，需指定签署方信息：姓名、手机号码、企业名称等，才能生成正确的跳转链接"
)
@AllArgsConstructor
public class ObtainTheSigningLinkToJumpToTencentsElectronicSignatureMiniProgramServiceImpl implements BaseCustomFunctionInterface<ObtainTheSigningLinkToJumpToTencentsElectronicSignatureMiniProgramDto> {

    @Override
    public Object execute(ObtainTheSigningLinkToJumpToTencentsElectronicSignatureMiniProgramDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateSchemeUrlRequest req = new CreateSchemeUrlRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setRecipientId(dto.getRecipientId());
            req.setPathType(dto.getPathType());
            req.setOrganizationName(dto.getOrganizationName());
            req.setName(dto.getName());
            req.setMobile(dto.getMobile());
            req.setHides(dto.getHides().toArray(new Long[dto.getHides().size()]));
            req.setFlowId(dto.getFlowId());
            req.setFlowGroupId(dto.getFlowGroupId());
            req.setEndPoint(dto.getEndPoint());
            req.setAutoJumpBack(dto.getAutoJumpBack());

            // 返回的resp是一个CreateSchemeUrlResponse的实例，与请求对象对应
            CreateSchemeUrlResponse resp = client.CreateSchemeUrl(req);

            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
