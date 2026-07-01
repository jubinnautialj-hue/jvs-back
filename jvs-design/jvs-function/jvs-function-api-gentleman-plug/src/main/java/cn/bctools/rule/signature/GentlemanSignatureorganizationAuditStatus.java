package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureorganizationAuditStatusDto;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.entity.enums.TestShowEnum;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import cn.bctools.rule.utils.JunZiQianUtils;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Gentleman signatureorganization audit status.
 *
 * @author jvs
 */
@Rule(value = "企业实名认证状态查询",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "查询是否审核成功。此接口为同步请求接口，也可以配置回调地址，由君子签异步返回审核结果。"
)
@AllArgsConstructor
public class GentlemanSignatureorganizationAuditStatus implements BaseCustomFunctionInterface<GentlemanSignatureorganizationAuditStatusDto> {

    @Override
    public Object execute(GentlemanSignatureorganizationAuditStatusDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/organizationAuditStatus", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureorganizationAuditStatusDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo status = new RuleElementVo().setName("status").setInfo("审批状态,0正在申请1通过2驳回").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(status);
        RuleElementVo msg = new RuleElementVo().setName("msg").setInfo("审批信息说明").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(msg);
        return list;
    }
}
