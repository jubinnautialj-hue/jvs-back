package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturegerenbiaozhuntuxingzhangxiazaiDto;
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
 * The type Gentleman signaturegerenbiaozhuntuxingzhangxiazai.
 *
 * @author jvs
 */
@Rule(value = "个人标准图形章下载",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "下载个人用户标准图形章"
)
@AllArgsConstructor
public class GentlemanSignaturegerenbiaozhuntuxingzhangxiazai implements BaseCustomFunctionInterface<GentlemanSignaturegerenbiaozhuntuxingzhangxiazaiDto> {

    @Override
    public Object execute(GentlemanSignaturegerenbiaozhuntuxingzhangxiazaiDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/user/persSignDown", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturegerenbiaozhuntuxingzhangxiazaiDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo signData = new RuleElementVo().setName("signData").setInfo("个人标准图形章（base64String字符串）").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(signData);
        return list;
    }
}
