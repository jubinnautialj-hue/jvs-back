package cn.bctools.rule.signature;

import cn.bctools.function.enums.JvsParamType;
import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.common.RuleElementVo;
import cn.bctools.rule.dto.GentlemanSignatureh5FaceVideoDownloadDto;
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
 * The type Gentleman signatureh 5 face video download.
 *
 * @author jvs
 */
@Rule(value = "人脸视频下载",
        group = RuleGroup.君子签,
        test = true,
        returnType = ClassType.对象,
        testShowEnum = TestShowEnum.JSON,
        order = 2,
        explain = "H5人脸认证后，君子签提供人脸视频的下载，建议收到人脸后等过半分钟再进行下载，视频只临时缓存3天，如有需要请及时拉取自行保存。"
)
@AllArgsConstructor
public class GentlemanSignatureh5FaceVideoDownload implements BaseCustomFunctionInterface<GentlemanSignatureh5FaceVideoDownloadDto> {

    @Override
    public Object execute(GentlemanSignatureh5FaceVideoDownloadDto dto, Map<String, Object> params) {
        return JunZiQianUtils.post("/v2/auth/h5FaceVideoDownload", dto);
    }

    @Override
    public List<RuleElementVo> structureType(GentlemanSignatureh5FaceVideoDownloadDto o) {
        List<RuleElementVo> list = new ArrayList<>();
        RuleElementVo faceVideo = new RuleElementVo().setName("faceVideo").setInfo("视频记录，为byte[]的Base64String").setJvsParamType(JvsParamType.text).setJvsParamTypeName(JvsParamType.text.getDesc());
        list.add(faceVideo);
        return list;
    }
}
