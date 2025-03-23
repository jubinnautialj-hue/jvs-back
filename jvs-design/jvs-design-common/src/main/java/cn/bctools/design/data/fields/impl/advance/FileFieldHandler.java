package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.FormFileDto;
import cn.bctools.design.data.fields.dto.form.html.FileHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.oss.template.OssTemplate;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@DesignField(value = "文件", type = DataFieldType.file)
@AllArgsConstructor
public class FileFieldHandler implements IDataFieldHandler<FileHtml> {

    OssTemplate ossTemplate;


    @Override
    public Object getEchoValue(FileHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
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
            }
        }
        return data;
    }

    /**
     * 获取文件桶名
     *
     * @param fileDto 文件数据对象
     * @return 桶名
     */
    private String getBucket(FormFileDto fileDto) {
        String fileName = fileDto.getFileName();
        String url = fileDto.getUrl();
        if (StringUtils.isBlank(fileName) || StringUtils.isBlank(url)) {
            return null;
        }
        String[] split = url.split(fileName, 2);
        if (ObjectUtils.isEmpty(split)) {
            return null;
        }
        String[] strArr = split[0].split("/");
        String bucket = null;
        // 获取最后一段字符串
        for (String s : strArr) {
            if (StringUtils.isNotBlank(s)) {
                bucket = s;
            }
        }
        return bucket;
    }

    @Override
    public void checkDataFieldType(FileHtml fileHtml, Object o) throws Exception {
        if (o instanceof Map || o instanceof List || o instanceof FileNameDto) {
        } else {
            throw new RuntimeException("正确格式为键值对");
        }
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"file\",\n" +
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
                "        \"jurisdiction\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"fileList\": [\n" +
                "        {\n" +
                "            \"name\": \"\",\n" +
                "            \"url\": \"\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"rules\": [\n" +
                "\n" +
                "    ],\n" +
                "    \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"name\": \"" + DataFieldType.file.getDesc() + "\",\n" +
                "    \"disabled\": false,\n" +
                "    \"defaultOrigin\": \"\"\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
