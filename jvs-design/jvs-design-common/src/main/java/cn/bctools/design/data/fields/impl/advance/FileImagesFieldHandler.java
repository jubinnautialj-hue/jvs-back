package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.ImagesHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.oss.template.OssTemplate;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表单字段: 文件,图片
 * <p>
 * 将文件的访问地址处理为可用的临时链接
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "图片", type = DataFieldType.image)
public class FileImagesFieldHandler implements IDataFieldHandler<ImagesHtml> {
    @Autowired
    OssTemplate ossTemplate;

    @Override
    public Object getEchoValue(ImagesHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
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
                                e.setUrl(ossTemplate.fileLink(e.getFileName(), e.getBucketName()));
                            }
                        })
                        .collect(Collectors.toList());
            }
        }
        return data;
    }

    @Override
    public void checkDataFieldType(ImagesHtml imagesHtml, Object o) throws Exception {
        if (o instanceof Map || o instanceof List || o instanceof FileNameDto) {
        } else {
            throw new RuntimeException("正确格式为键值对");
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"image\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"showFrom\": [\n" +
                "        \"label\",\n" +
                "        \"span\",\n" +
                "        \"prop\",\n" +
                "        \"url\",\n" +
                "        \"fit\",\n" +
                "        \"jurisdiction\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"url\": \"\",\n" +
                "    \"fit\": \"\",\n" +
                "    \"fileList\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"dialogImageUrl\": \"\",\n" +
                "    \"dialogVisible\": false,\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.image.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
