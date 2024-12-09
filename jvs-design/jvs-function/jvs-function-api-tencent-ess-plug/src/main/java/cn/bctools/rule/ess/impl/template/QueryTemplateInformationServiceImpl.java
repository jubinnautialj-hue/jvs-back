package cn.bctools.rule.ess.impl.template;

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
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowTemplatesRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowTemplatesResponse;
import com.tencentcloudapi.ess.v20201111.models.Filter;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询模板信息-电子签",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "适用场景 该接口常用来配合模板发起合同-创建电子文档接口，作为创建电子文档的前置接口使用。 通过此接口查询到模板信息后，再通过调用创建电子文档接口，指定模板ID，指定模板中需要的填写控件内容等，完成电子文档的创建。"
)
@AllArgsConstructor
public class QueryTemplateInformationServiceImpl implements BaseCustomFunctionInterface<QueryTemplateInformationDto> {

    @Override
    public Object execute(QueryTemplateInformationDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeFlowTemplatesRequest req = new DescribeFlowTemplatesRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            try {
                req.setFilters(BeanCopyUtil.copys(dto.getFilters(), Filter.class).toArray(new Filter[dto.getFilters().size()]));
            } catch (Exception ignored) {

            }
            req.setLimit(dto.getLimit());
            req.setApplicationId(dto.getApplicationId());
            req.setContentType(dto.getContentType());
            req.setWithPreviewUrl(dto.getWithPreviewUrl());
            req.setOffset(dto.getOffset());
            // 返回的resp是一个DescribeFlowTemplatesResponse的实例，与请求对象对应
            DescribeFlowTemplatesResponse resp = client.DescribeFlowTemplates(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }
    }
}
