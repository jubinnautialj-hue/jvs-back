package cn.bctools.rule.tools.http;

import cn.bctools.rule.common.ParameterOption;
import cn.bctools.rule.common.ParameterSelected;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * The type Media type selected.
 */
@Service
public class MediaTypeSelected implements ParameterSelected<String> {

    /**
     * 获取请求方法类型
     *
     * @return {@linkplain List < ParameterOption > }
     */
    @Override
    public List<ParameterOption<String>> getOptions() {
        return Arrays.asList(
                new ParameterOption<String>().setLabel(MediaType.APPLICATION_JSON_VALUE).setValue(MediaType.APPLICATION_JSON_VALUE),
                new ParameterOption<String>().setLabel(MediaType.MULTIPART_FORM_DATA_VALUE).setValue(MediaType.MULTIPART_FORM_DATA_VALUE),
                new ParameterOption<String>().setLabel(MediaType.APPLICATION_FORM_URLENCODED_VALUE).setValue(MediaType.APPLICATION_FORM_URLENCODED_VALUE),
                new ParameterOption<String>().setLabel(MediaType.APPLICATION_XML_VALUE).setValue(MediaType.APPLICATION_XML_VALUE),
                new ParameterOption<String>().setLabel(MediaType.TEXT_PLAIN_VALUE).setValue(MediaType.TEXT_PLAIN_VALUE),
                new ParameterOption<String>().setLabel("application/binary").setValue("application/binary"),
                new ParameterOption<String>().setLabel(MediaType.APPLICATION_OCTET_STREAM_VALUE).setValue(MediaType.APPLICATION_OCTET_STREAM_VALUE)
        );
    }

    @Override
    public Object getDefaultValueParameter() {
        //默认获取第0个
        return getOptions().get(0).getValue();
    }

}
