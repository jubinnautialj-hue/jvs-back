package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.ImageUploadHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 */
@Slf4j
@Component
@DesignField(value = "手写签名", type = DataFieldType.signature)
@AllArgsConstructor
public class SignatureFieldHandler implements IDataFieldHandler<ImageUploadHtml> {

    OssTemplate ossTemplate;

    @Override
    public Object getEchoValue(ImageUploadHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        //处理公共桶图片兼容显示问题
        if (ObjectNull.isNotNull(data)) {
            if (data instanceof List) {
                return JSONArray.parseArray(JSONObject.toJSONString(data), BaseFile.class)
                        .stream()
                        .filter(ObjectNull::isNotNull)
                        .peek(e -> {
                            if (ObjectNull.isNull(e.getName())) {
                                e.setName(e.getOriginalName());
                            }
                            if (ObjectNull.isNotNull(e.getBucketName(), e.getFileName())) {
                                e.setUrl(ossTemplate.fileLink(e.getFileName(), Optional.ofNullable(e.getBucketName()).orElseGet(() -> "jvs-form-design")));
                            }
                        })
                        .collect(Collectors.toList());
            } else if (data instanceof String) {
                return Arrays.asList(new BaseFile().setUrl(data.toString()).setBucketName("jvs-form-design"));
            }
        }
        return data;
    }

    @Override
    public Object checkDataFieldType(ImageUploadHtml imageUploadHtml, Object o) throws Exception {
        if (!(o instanceof List)) {
            throw new RuntimeException("正确格式为数组");
        }
        return o;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"signature\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"showFrom\": [\n" +
                "        \"prop\",\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"disabled\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"disabled\": false,\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.signature.getDesc() + "\",\n" +
                "    \"sqlType\": \"varchar\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
