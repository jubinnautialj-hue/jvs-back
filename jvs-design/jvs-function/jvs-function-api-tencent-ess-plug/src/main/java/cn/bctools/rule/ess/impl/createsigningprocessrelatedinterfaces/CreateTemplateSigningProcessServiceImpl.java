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
import com.tencentcloudapi.ess.v20201111.models.*;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gl
 */
@Rule(value = "模板发起合同-创建签署流程",
        group = RuleGroup.腾讯电子签,
        test = true,
        returnType = ClassType.对象,

        testShowEnum = TestShowEnum.TEXT,
        order = 2,
//        iconUrl = "rule-dysmsduanxinfuwu",
        explain = "适用场景：在标准制式的合同场景中，可通过提前预制好模板文件，每次调用模板文件的id，补充合同内容信息及签署信息生成电子合同。"
)
@AllArgsConstructor
public class CreateTemplateSigningProcessServiceImpl implements BaseCustomFunctionInterface<CreateTemplateSigningProcessDto> {

    @Override
    public Object execute(CreateTemplateSigningProcessDto dto, Map<String, Object> params) {
        EssClient client = TenantUtil.getClient(dto.getOptions());
        CreateFlowRequest req = new CreateFlowRequest();
        try {
            req.setOperator(BeanCopyUtil.copy(dto.getOperator(), UserInfo.class));
            req.setFlowName(dto.getFlowName());
            try {
                FlowCreateApprover[] array = BeanCopyUtil.copys(dto.getApprovers(), FlowCreateApprover.class).toArray(new FlowCreateApprover[dto.getApprovers().size()]);
                req.setApprovers(array);
            } catch (Exception ignored) {

            }
            req.setFlowDescription(dto.getFlowDescription());
            req.setFlowType(dto.getFlowType());
            req.setDeadLine(dto.getDeadLine());
            req.setRemindedOn(dto.getRemindedOn());
            req.setUserData(dto.getUserData());
            req.setUnordered(dto.getUnordered());
            req.setCustomShowMap(dto.getCustomShowMap());
            req.setNeedSignReview(dto.getNeedSignReview());
            try {
                CcInfo[] infos = (CcInfo[]) dto.getCcInfos().stream().map(e -> BeanCopyUtil.copy(e, CcInfo.class)).toArray();
                req.setCcInfos(infos);
            } catch (Exception ignored) {

            }
            req.setAutoSignScene(dto.getAutoSignScene());
            CreateFlowResponse resp = client.CreateFlow(req);
            return BeanCopyUtil.copy(resp, HashMap.class);
        } catch (TencentCloudSDKException e) {
            LOG.error(e);
            throw new RuleException(RuleExceptionEnum.三方平台报错, SpringContextUtil.msg("三方平台执行异常具体信息错误码", e.getMessage(), e.getRequestId(), e.getErrorCode()));

        }
    }
}
