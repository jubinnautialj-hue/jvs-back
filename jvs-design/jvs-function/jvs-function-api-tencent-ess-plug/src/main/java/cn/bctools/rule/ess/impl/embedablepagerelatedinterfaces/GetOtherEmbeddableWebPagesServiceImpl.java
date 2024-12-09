package cn.bctools.rule.ess.impl.embedablepagerelatedinterfaces;

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
import com.tencentcloudapi.ess.v20201111.models.CreateEmbedWebUrlRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateEmbedWebUrlResponse;
import com.tencentcloudapi.ess.v20201111.models.ReviewerInfo;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "获取其他可嵌入web页面", group = RuleGroup.腾讯电子签, test = true, enable = true, returnType = ClassType.对象, testShowEnum = TestShowEnum.JSON, order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "本接口（CreateEmbedWebUrl）用于创建嵌入Web的链接，支持以下类型的Web链接创建：\n" + "\n" + "创建印章\n" + "创建模板\n" + "修改模板\n" + "预览模板\n" + "预览合同流程\n" + "用户可以通过这些链接快速将其集成到自己的系统中。")
@AllArgsConstructor
public class GetOtherEmbeddableWebPagesServiceImpl implements BaseCustomFunctionInterface<GetOtherEmbeddableWebPagesDto> {

    @Override
    public Object execute(GetOtherEmbeddableWebPagesDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        // 实例化一个请求对象,每个接口都会对应一个request对象
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateEmbedWebUrlRequest req = new CreateEmbedWebUrlRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setBusinessId(dto.getBusinessId());
            req.setEmbedType(dto.getEmbedType());
            req.setReviewer(BeanCopyUtil.copy(dto.getReviewer(), ReviewerInfo.class));
            // 返回的resp是一个CreateEmbedWebUrlResponse的实例，与请求对象对应
            CreateEmbedWebUrlResponse resp = client.CreateEmbedWebUrl(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }

    }
}
