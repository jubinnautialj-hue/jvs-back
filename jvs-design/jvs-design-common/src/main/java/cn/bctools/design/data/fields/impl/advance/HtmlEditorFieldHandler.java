package cn.bctools.design.data.fields.impl.advance;

import cn.bctools.design.data.fields.DesignField;
import cn.bctools.design.data.fields.IDataFieldHandler;
import cn.bctools.design.data.fields.dto.form.html.HtmlEditorHtml;
import cn.bctools.design.data.fields.enums.DataFieldType;
import cn.bctools.oss.props.OssProperties;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表单字段: 富文本
 *
 * @Author: GuoZi
 */
@Slf4j
@Component
@DesignField(value = "富文本", type = DataFieldType.htmlEditor)
@AllArgsConstructor
public class HtmlEditorFieldHandler implements IDataFieldHandler<HtmlEditorHtml> {

    OssProperties ossProperties;

    @Override
    public Object getEchoValue(HtmlEditorHtml fieldDto, Object data, boolean override, Map<String, Object> lineData, String... paths) {
        //需要对内容进行格式化处理
        return replaceHtmlBodySrc((String) data, ossProperties.getEndpoint());
    }

    /**
     * 将富文本的 src进行替换处理了
     *
     * @param htmlBody 富文本内容
     * @param oss
     * @return
     */
    public static String replaceHtmlBodySrc(String htmlBody, String oss) {
// 正则表达式匹配 src 属性内容
        String regex = "src=\"([^\"]+)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(htmlBody);
// 循环匹配结果
        while (matcher.find()) {
            // 获取第一个分组内容，即 src 属性的值
            String src = matcher.group(1);
            if (oss.startsWith("http") && src.startsWith(oss)) {
                continue;
            }
            if (src.startsWith("http://" + oss)) {
                continue;
            }
            htmlBody = htmlBody.replaceAll(src, oss.trim().startsWith("http") ? oss + src : "http://" + oss + src);
        }
        return htmlBody;
    }

    @Override
    public Map<String, Object> generate(String name, String field, List<String> dicData) {
        String str = "{\n" +
                "    \"prop\": \"" + field + "\",\n" +
                "    \"type\": \"htmlEditor\",\n" +
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
                "        \"jurisdiction\",\n" +
                "        \"disabled\",\n" +
                "        \"span\",\n" +
                "        \"sqlType\"\n" +
                "    ],\n" +
                "    \"showJurisdiction\": [\n" +
                "        \"所有用户\"\n" +
                "    ],\n" +
                "    \"sqlType\": \"varchar\",\n" +
                "    \"linkbind\": \"\",\n" +
                "    \"rules\": [\n" +
                "        {\n" +
                "            \"required\": false,\n" +
                "            \"message\": \"请输入" + name + "\",\n" +
                "            \"trigger\": \"change\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"name\": \"" + DataFieldType.htmlEditor.getDesc() + "\",\n" +
                "    \"disabled\": false\n" +
                "}";
        return JSONObject.parseObject(str);
    }
}
