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
import com.tencentcloudapi.ess.v20201111.models.CreateFlowSignUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowSignUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.FlowCreateApprover;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "获取个人用户H5签署链接",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "该接口用于发起合同后，生成个人用户的签署链接, 暂时不支持企业端签署\n" +
                "\n" +
                "注意\n" +
                "\n" +
                "该接口目前仅支持签署人类型是个人签署方的场景(PERSON)。\n" +
                "该接口可生成签署链接的C端签署人必须仅有手写签名和时间类型的签署控件，不支持填写控件 。\n" +
                "该签署链接有效期为30分钟，过期后将失效，如需签署可重新创建签署链接 。\n" +
                "该接口返回的签署链接适用于APP集成的场景，支持APP打开或浏览器直接打开，不支持微信小程序嵌入。 跳转到小程序的实现，参考微信官方文档（分为全屏、半屏两种方式），如何配置也可以请参考: 跳转电子签小程序配置。\n" +
                "因h5涉及人脸身份认证能力基于慧眼人脸核身，对Android和iOS系统均有一定要求， 因此App嵌入H5签署合同需要按照慧眼提供的慧眼人脸核身兼容性文档做兼容性适配。"
)
@AllArgsConstructor
public class ObtainIndividualUserH5SigningLinkServiceImpl implements BaseCustomFunctionInterface<ObtainIndividualUserH5SigningLinkDto> {

    @Override
    public Object execute(ObtainIndividualUserH5SigningLinkDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateFlowSignUrlRequest req = new CreateFlowSignUrlRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowId(dto.getFlowId());
            req.setJumpUrl(dto.getJumpUrl());
            try {
                req.setFlowApproverInfos(BeanCopyUtil.copys(dto.getFlowApproverInfos(), FlowCreateApprover.class).toArray(new FlowCreateApprover[dto.getFlowApproverInfos().size()]));
            } catch (Exception ignored) {

            }
            // 返回的resp是一个CreateFlowSignUrlResponse的实例，与请求对象对应
            CreateFlowSignUrlResponse resp = client.CreateFlowSignUrl(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
