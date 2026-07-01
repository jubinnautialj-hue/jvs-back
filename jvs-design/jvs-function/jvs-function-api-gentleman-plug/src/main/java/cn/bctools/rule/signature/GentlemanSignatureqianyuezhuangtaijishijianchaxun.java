package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureqianyuezhuangtaijishijianchaxunDto;
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
 * The type Gentleman signatureqianyuezhuangtaijishijianchaxun.
 *
 * @author jvs
 */
@Rule(value = "签约状态及时间查询",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "查询签约人签字状态及签字时间,此接口签约人信息必传”"
)
@AllArgsConstructor
public class GentlemanSignatureqianyuezhuangtaijishijianchaxun implements BaseCustomFunctionInterface<GentlemanSignatureqianyuezhuangtaijishijianchaxunDto> {

    @Override
    public Object execute(GentlemanSignatureqianyuezhuangtaijishijianchaxunDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/sign/info", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureqianyuezhuangtaijishijianchaxunDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo signStatus = new RuleElementVo().setName("signStatus").setInfo("0 ：未签 1 ：已签 2：拒签 3：已保全").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(signStatus);
        RuleElementVo signTime = new RuleElementVo().setName("signTime").setInfo("签字时间").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(signTime);
        RuleElementVo users = new RuleElementVo().setName("users").setInfo("签约人列表").setJvsParamType(JvsParamType.array).setJvsParamTypeName(JvsParamType.array.getDesc());
        ArrayList<RuleElementVo> childrenUsers = new ArrayList<>();
        childrenUsers.add(new RuleElementVo().setName("fullName").setInfo("名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        childrenUsers.add(new RuleElementVo().setName("identityType").setInfo("证件类型").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc()));
        childrenUsers.add(new RuleElementVo().setName("identityCard").setInfo("证件号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc()));
        childrenUsers.add(new RuleElementVo().setName("signStatus").setInfo("签约状态").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc()));
        childrenUsers.add(new RuleElementVo().setName("signTime").setInfo("签字时间").setJvsParamType(JvsParamType.date).setJvsParamTypeName(JvsParamType.date.getDesc()));
        users.setChildren(childrenUsers);
        list.add(users);
        return list;
    }
}
