package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignaturestartH5FaceDto;
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
 * The type Gentleman signaturestart h 5 face.
 *
 * @author jvs
 */
@Rule(value = "H5人脸认证",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "君子签提供单独的H5人脸识别服务，该接口主要用于个人进行人脸识别，确保是否是本人。该接口涉及到人脸识别费用，如果未购买需要联系商务进行购买。\n" +
                "\n" +
                "注：获取到人脸URL地址后，链接地址有效期30分钟，一个链接只能打开一次，不管是否验证再次打开同一链接则失效，需要重新获取新的人脸URL地址。“"
)
@AllArgsConstructor
public class GentlemanSignaturestartH5Face implements BaseCustomFunctionInterface<GentlemanSignaturestartH5FaceDto> {

    @Override
    public Object execute(GentlemanSignaturestartH5FaceDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/startH5Face", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignaturestartH5FaceDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo startFaceUrl = new RuleElementVo().setName("startFaceUrl").setInfo("人脸验证启动url").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(startFaceUrl);
        return list;
    }
}
