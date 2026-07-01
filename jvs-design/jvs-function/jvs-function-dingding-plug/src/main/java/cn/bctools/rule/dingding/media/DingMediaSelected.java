package cn.bctools.rule.dingding.media;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * The type Ding media selected.
 *
 * @author jvs
 */
@Service
public class DingMediaSelected implements ParameterSelected<String> {

    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.asList(
                new ParameterOption<String>().setLabel("图片，图片最大20MB。支持上传jpg、gif、png、bmp格式").setValue("image"),
                new ParameterOption<String>().setLabel("语音，语音文件最大2MB。支持上传amr、mp3、wav格式").setValue("voice"),
                new ParameterOption<String>().setLabel("视频，视频最大20MB。支持上传mp4格式").setValue("video"),
                new ParameterOption<String>().setLabel("普通文件，最大20MB。支持上传doc、docx、xls、xlsx、ppt、pptx、zip、pdf、rar格式").setValue("file")
        );
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

}
