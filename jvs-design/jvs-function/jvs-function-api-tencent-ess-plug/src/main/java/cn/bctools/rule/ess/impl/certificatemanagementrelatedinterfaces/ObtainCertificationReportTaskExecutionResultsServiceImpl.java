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
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowEvidenceReportRequest;
import com.tencentcloudapi.ess.v20201111.models.DescribeFlowEvidenceReportResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "获取出证报告任务执行结果",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "获取出证报告任务执行结果，返回报告 URL。\n" +
                "\n" +
                "注意：\n" +
                "\n" +
                "使用此功能`需搭配出证套餐` ，使用前请联系对接的客户经理沟通。\n" +
                "需调用创建并返回出证报告接口提交申请出证报告任务获取报告编号后调用当前接口获取报告链接。"
)
@AllArgsConstructor
public class ObtainCertificationReportTaskExecutionResultsServiceImpl implements BaseCustomFunctionInterface<ObtainCertificationReportTaskExecutionResultsDto> {

    @Override
    public Object execute(ObtainCertificationReportTaskExecutionResultsDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeFlowEvidenceReportRequest req = BeanCopyUtil.copy(dto, DescribeFlowEvidenceReportRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));

            // 返回的resp是一个DescribeFlowEvidenceReportResponse的实例，与请求对象对应
            DescribeFlowEvidenceReportResponse resp = client.DescribeFlowEvidenceReport(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
