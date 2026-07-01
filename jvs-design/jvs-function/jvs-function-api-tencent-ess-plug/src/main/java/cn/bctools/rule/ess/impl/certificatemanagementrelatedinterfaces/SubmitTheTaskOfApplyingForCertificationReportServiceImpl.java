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
import com.tencentcloudapi.ess.v20201111.models.CreateFlowEvidenceReportRequest;
import com.tencentcloudapi.ess.v20201111.models.CreateFlowEvidenceReportResponse;
import com.tencentcloudapi.ess.v20201111.models.UserInfo;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "提交申请出证报告任务",
        group = RuleGroup.腾讯电子签,
        test = true,
        enable = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "提交申请出证报告任务并返回报告ID。\n" +
                "\n" +
                "注意：\n" +
                "\n" +
                "使用此功能`需搭配出证套餐` ，使用前请联系对接的客户经理沟通。\n" +
                "操作人必须是`发起方或者签署方企业的(非走授权书认证)法人或者超管`。\n" +
                "合同流程必须所有参与方`已经签署完成`。\n" +
                "出证过程需一定时间，建议在`提交出证任务后的24小时之后`，通过获取出证报告任务执行结果接口进行查询执行结果和出证报告的下载URL。"
)
@AllArgsConstructor
public class SubmitTheTaskOfApplyingForCertificationReportServiceImpl implements BaseCustomFunctionInterface<SubmitTheTaskOfApplyingForCertificationReportDto> {

    @Override
    public Object execute(SubmitTheTaskOfApplyingForCertificationReportDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        try {
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreateFlowEvidenceReportRequest req = BeanCopyUtil.copy(dto, CreateFlowEvidenceReportRequest.class);
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));

            // 返回的resp是一个CreateFlowEvidenceReportResponse的实例，与请求对象对应

            CreateFlowEvidenceReportResponse resp = client.CreateFlowEvidenceReport(req);
            return BeanCopyUtil.copy(resp, HashMap.class);

        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }

    }
}
