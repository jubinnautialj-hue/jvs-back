package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.ImageUploadHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.function.entity.vo.ElementVo;
import cn.bctools.function.enums.JvsParamType;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.dto.FileNameDto;
import cn.bctools.oss.template.OssTemplate;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@DesignField(value = "上传图片", type = DataFieldType.imageUpload)
@AllArgsConstructor
public class FileImageUploadFieldHandler implements IDataFieldHandler<ImageUploadHtml> {
    OssTemplate ossTemplate;

    /**
     * 进行数据解析自定义嵌套用于公式处理
     */
    @Override
    public void next(List<ElementVo> list, ImageUploadHtml publicHtml, Map<String, IDataFieldHandler> handlerMap, ElementVo e) {
        //解析固定字段进行处理
        ElementVo htmlName = new ElementVo()
                .setId(e.getId() + ".name")
                .setPath(e.getPath() + ".name")
                .setShortName(e.getShortName() + ".文件名")
                .setName(publicHtml.getName() + ".文件名")
                .setJvsParamType(JvsParamType.text);
        list.add(htmlName.setInfo(e.getName() + "文件上传的名称:" + htmlName.getId()));
        ElementVo url = new ElementVo()
                .setId(e.getId() + ".url")
                .setPath(e.getPath() + ".name")
                .setShortName(e.getShortName() + ".文件链接")
                .setName(publicHtml.getName() + ".文件链接")
                .setJvsParamType(JvsParamType.text);
        list.add(url.setInfo(e.getName() + "文件链接:" + url.getId()));
    }

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
            }
        }
        return data;
    }

    @Override
    public void checkDataFieldType(ImageUploadHtml imagesHtml, Object o) throws Exception {
        if (o instanceof Map || o instanceof List || o instanceof FileNameDto) {
        } else {
            throw new RuntimeException("正确格式为键值对");
        }
    }


    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"imageUpload\",\n" +
                "    \"label\": \"" + name + "\",\n" +
                "    \"span\": 24,\n" +
                "    \"display\": true,\n" +
                "    \"status\": \"\",\n" +
                "    \"tips\": {\n" +
                "        \"text\": \"\",\n" +
                "        \"position\": \"right\"\n" +
                "    },\n" +
                "    \"limit\": 1,\n" +
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
                "    \"fileList\": [],\n" +
                "    \"rules\": [],\n" +
                "    \"dialogImageUrl\": \"\",\n" +
                "    \"dialogVisible\": false,\n" +
                "    \"sqlType\": \"array\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"uploadHttp\": {\n" +
                "      \"httpMethod\": \"POST\",\n" +
                "      \"requestContentType\": \"MULTIPART\",\n" +
                "      \"responseContentType\": \"JSON\",\n" +
                "      \"url\": \"/mgr/jvs-auth/upload/jvs-form-design\"\n" +
                "    }," +
                "    \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.imageUpload.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
