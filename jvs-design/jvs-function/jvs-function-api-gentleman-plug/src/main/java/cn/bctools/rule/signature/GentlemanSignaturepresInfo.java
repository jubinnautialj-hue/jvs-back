package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturepresInfoDto;
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
 * The type Gentleman signaturepres info.
 *
 * @author jvs
 */
@Rule(value = "查询存证信息",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "通过此接口可以查询在易保全存证的合同及其他文件的保全信息"
)
@AllArgsConstructor
public class GentlemanSignaturepresInfo implements BaseCustomFunctionInterface<GentlemanSignaturepresInfoDto> {

    @Override
    public Object execute(GentlemanSignaturepresInfoDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/pre/info", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturepresInfoDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo preservationId = new RuleElementVo().setName("preservationId").setInfo("保全备案号").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(preservationId);
        RuleElementVo operatorEntityId = new RuleElementVo().setName("operatorEntityId").setInfo("操作用户证件号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(operatorEntityId);
        RuleElementVo operatorEntityName = new RuleElementVo().setName("operatorEntityName").setInfo("操作用户名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(operatorEntityName);
        RuleElementVo entityId = new RuleElementVo().setName("entityId").setInfo("存证主体证件号").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(entityId);
        RuleElementVo entityName = new RuleElementVo().setName("entityName").setInfo("存证主体名称").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(entityName);
        RuleElementVo preservationTime = new RuleElementVo().setName("preservationTime").setInfo("存证时间戳").setJvsParamType(JvsParamType.number).setJvsParamTypeName(JvsParamType.number.getDesc());
        list.add(preservationTime);
        RuleElementVo preservationDeceive = new RuleElementVo().setName("preservationDeceive").setInfo("存证环境信息").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(preservationDeceive);
        RuleElementVo sha512Hash = new RuleElementVo().setName("sha512Hash").setInfo("存证SHA512值").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(sha512Hash);
        RuleElementVo sha256Hash = new RuleElementVo().setName("sha256Hash").setInfo("存证SHA256值").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(sha256Hash);
        RuleElementVo ebqChainTransHash = new RuleElementVo().setName("ebqChainTransHash").setInfo("易保全链交易哈希").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(ebqChainTransHash);
        RuleElementVo gznetId = new RuleElementVo().setName("gznetId").setInfo("广州互联网法院证据ID").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(gznetId);
        RuleElementVo antId = new RuleElementVo().setName("antId").setInfo("杭州互联网法院证据ID").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(antId);
        RuleElementVo humiId = new RuleElementVo().setName("humiId").setInfo("工信部标识ID").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(humiId);
        RuleElementVo sifaId = new RuleElementVo().setName("sifaId").setInfo("司法链ID").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(sifaId);
        return list;
    }
}
