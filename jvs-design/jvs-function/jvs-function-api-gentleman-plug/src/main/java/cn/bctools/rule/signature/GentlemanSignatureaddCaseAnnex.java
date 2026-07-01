package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureaddCaseAnnexDto;
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
 * The type Gentleman signatureadd case annex.
 *
 * @author jvs
 */
@Rule(value = "仲裁查询",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过此接口可以查询到仲裁申请详情"
)
@AllArgsConstructor
public class GentlemanSignatureaddCaseAnnex implements BaseCustomFunctionInterface<GentlemanSignatureaddCaseAnnexDto> {

    @Override
    public Object execute(GentlemanSignatureaddCaseAnnexDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/ebqarb/query", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureaddCaseAnnexDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo total = new RuleElementVo().setName("total").setInfo("总数").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(total);
        RuleElementVo pageSize = new RuleElementVo().setName("pageSize").setInfo("每页数量").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(pageSize);
        RuleElementVo pageNum = new RuleElementVo().setName("pageNum").setInfo("页码").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(pageNum);
        RuleElementVo pages = new RuleElementVo().setName("pages").setInfo("页数量").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(pages);
        RuleElementVo preservationId = new RuleElementVo().setName("preservationId").setInfo("备案号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(preservationId);
        RuleElementVo presName = new RuleElementVo().setName("presName").setInfo("保全名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(presName);
        RuleElementVo fileNumber = new RuleElementVo().setName("fileNumber").setInfo("文件编号(合同发起接口中返回的APL编号)").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(fileNumber);
        RuleElementVo signStatus = new RuleElementVo().setName("signStatus").setInfo("合同签署状态").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(signStatus);
        RuleElementVo orderNo = new RuleElementVo().setName("orderNo").setInfo("仲裁订单编号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(orderNo);
        RuleElementVo createTime = new RuleElementVo().setName("createTime").setInfo("创建仲裁申请时间").setJvsParamType(JvsParamType.date).setJvsParamTypeName(JvsParamType.date.getDesc());
        list.add(createTime);
        RuleElementVo status = new RuleElementVo().setName("status").setInfo("仲裁状态（1:未提交 2:待接受 3:预审中 4:已立案 5:答辩期 6:审理期 7:已驳回 8:已拒绝 9:已结案）").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(status);
        RuleElementVo dtos = new RuleElementVo().setName("proposerDTOS").setInfo("申请人信息,参考后面proposerDTOS字段说明").setJvsParamType(JvsParamType.array).setJvsParamTypeName(JvsParamType.array.getDesc());
        ArrayList<RuleElementVo> childrenProposer = new ArrayList<>();
        childrenProposer.add(new RuleElementVo().setName("name").setInfo("名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        dtos.setChildren(childrenProposer);
        list.add(dtos);
        RuleElementVo agent = new RuleElementVo().setName("agent").setInfo("代理人信息,参考后面agentDTOS字段说明").setJvsParamType(JvsParamType.array).setJvsParamTypeName(JvsParamType.array.getDesc());
        ArrayList<RuleElementVo> childrenAgent = new ArrayList<>();
        childrenAgent.add(new RuleElementVo().setName("name").setInfo("名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        agent.setChildren(childrenAgent);
        list.add(agent);
        RuleElementVo respondent = new RuleElementVo().setName("respondentDTOS").setInfo("被申请人信息").setJvsParamType(JvsParamType.array).setJvsParamTypeName(JvsParamType.array.getDesc());
        ArrayList<RuleElementVo> childrenRespondent = new ArrayList<>();
        childrenRespondent.add(new RuleElementVo().setName("name").setInfo("名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        respondent.setChildren(childrenRespondent);
        list.add(respondent);
        return list;
    }
}
