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
import com.tencentcloudapi.ess.v20201111.models.DescribeFileUrlsRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFileUrlsResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "查询文件下载URL",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "本接口（DescribeFileUrls）用于查询文件的下载URL。 适用场景：通过传参合同流程编号，下载对应的合同PDF文件流到本地。\n"
)
@AllArgsConstructor
public class QueryFileDownloadURLServiceImpl implements BaseCustomFunctionInterface<QueryFileDownloadURLDto> {

    @Override
    public Object execute(QueryFileDownloadURLDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeFileUrlsRequest req = new DescribeFileUrlsRequest();
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFileName(dto.getFileName());
            req.setFileType(dto.getFileType());
            req.setBusinessIds(dto.getBusinessIds().toArray(new String[0]));
            req.setBusinessType(dto.getBusinessType());
            req.setLimit(dto.getLimit());
            req.setOffset(dto.getOffset());
            req.setUrlTtl(dto.getUrlTtl());
            // 返回的resp是一个DescribeFileUrlsResponse的实例，与请求对象对应
            DescribeFileUrlsResponse resp = client.DescribeFileUrls(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));
        }
    }

}
