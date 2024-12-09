package cn.bctools.design.notice.handler.util;

import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import cn.bctools.design.util.DynamicDataUtils;
import cn.bctools.design.notice.entity.enums.NoticeTypeEnum;
import cn.hutool.core.text.CharPool;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * 通知变量工具
 */
public class NoticeVariableUtils {
    private NoticeVariableUtils() {

    }

    /**
     * 用于替换数据的html属性。 此属性的值为数据模型的字段key
     */
    private static final String REPLACE_HTML_ATTR = "title";
    /**
     * 占位符
     */
    private static final String PLACEHOLDER_HTML_CLASS = "space";


    /**
     * 内容替换
     *
     * @param html 待替换的内容html
     * @param data 数据
     * @return 替换后的数据（纯文本）
     */
    public static String replacementText(String html, Map<String, Object> data) {
        if (StringUtils.isBlank(html)) {
            return null;
        }
        Document document = replacement(html, data);
        return document.text();
    }

    /**
     * 内容替换
     *
     * @param type 消息类型
     * @param html 待替换的内容html
     * @param data 数据
     * @return 替换后的数据（根据消息类型处理）
     */
    public static String replacement(NoticeTypeEnum type, String html, Map<String, Object> data) {
        if (StringUtils.isBlank(html)) {
            return null;
        }
        Document document = replacement(html, data);
        // 需要html标签的通知类型
        if (NoticeTypeEnum.SYSTEM.equals(type) || NoticeTypeEnum.EMAIL.equals(type)) {
            return document.getElementsByTag("body").html();
        }
        // 不需要html标签且支持\n换行符的通知类型
        StringBuilder text = new StringBuilder();
        document.getElementsByTag("p").forEach(p -> text.append(p.text()).append(CharPool.LF));
        return text.toString();
    }

    /**
     * 内容替换
     *
     * @param html 待替换的内容html
     * @param data 数据
     * @return Document
     */
    public static Document replacement(String html, Map<String, Object> data) {
        Document document = Jsoup.parse(html);
        // 替换变量
        Elements elements = document.getElementsByAttribute(REPLACE_HTML_ATTR);
        elements.forEach(element -> {
            String fieldKey = element.attr(REPLACE_HTML_ATTR);
            element.text(Optional.ofNullable(getValue(fieldKey, data)).orElse(""));
        });
        // 去除默认占位空格
        Elements placeholderElements = document.getElementsByClass(PLACEHOLDER_HTML_CLASS);
        placeholderElements.forEach(element -> {
            String spaceHtml = element.html();
            int firstSpaceIndexOf = spaceHtml.indexOf("&nbsp;");
            if (spaceHtml.startsWith("&nbsp;") && firstSpaceIndexOf != -1) {
                element.html(spaceHtml.substring(firstSpaceIndexOf + 6));
            }
        });
        return document;
    }

    /**
     * 模板变量替换
     *
     * @param templateVariable 模板变量
     * @param data             数据
     * @return
     */
    public static Map<String, Map<String, String>> replacementTemplateVariable(Map<String, Map<String, String>> templateVariable, Map<String, Object> data) {
        if (MapUtils.isEmpty(templateVariable)) {
            return Collections.emptyMap();
        }
        return templateVariable.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getValue(e.getValue(), data)));
    }


    /**
     * 得到值
     *
     * @param variable 具体的模板变量和变量
     * @param data     数据
     * @return
     */
    private static Map<String, String> getValue(Map<String, String> variable, Map<String, Object> data) {
        if (MapUtils.isEmpty(variable)) {
            return Collections.emptyMap();
        }
        return variable.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> getValue(e.getValue(), data)));
    }

    /**
     * 得到值
     *
     * @param key  具体变量或字符串
     * @param data 数据
     * @return 值
     */
    private static String getValue(String key, Map<String, Object> data) {
        // 获取显示值（字段_1）有数据，标识有显示值
        String echoKey = DynamicDataUtils.getEchoFieldKey(key);
        Object echoValue = JvsJsonPath.read((data), echoKey);
        if (ObjectNull.isNotNull(echoValue)) {
            return String.valueOf(echoValue);
        }
        // 回显值为空，获取字段值
        Object read = JvsJsonPath.read((data), key);
        if (read instanceof JSONArray) {
            Integer index = SystemThreadLocal.get("index");
            if (ObjectNull.isNull(index)) {
                return String.valueOf(read);
            }
            return String.valueOf(((JSONArray) read).get(index));
        }
        return ObjectNull.isNull(read) ? null : String.valueOf(read);
    }
}
