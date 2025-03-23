package cn.bctools.data.factory.notice.utils;

import cn.bctools.data.factory.notice.enums.NoticeTypeEnum;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NoticeUtil {

    /**
     * 用于替换数据的html属性。 此属性的值为数据模型的字段key
     */
    private static final String REPLACE_HTML_ATTR = "title";

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

    private static String getValue(String key, Map<String, Object> data) {
        return StrUtil.toString(data.getOrDefault(key, ""));
    }

    /**
     * 内容替换
     *
     * @param html 待替换的内容html
     * @param data 数据
     * @return Document
     */
    private static Document replacement(String html, Map<String, Object> data) {
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByAttribute(REPLACE_HTML_ATTR);
        elements.forEach(element -> {
            String fieldKey = element.attr(REPLACE_HTML_ATTR);
            element.text(Optional.ofNullable(data.get(fieldKey)).map(StrUtil::toStringOrNull).orElse(""));
        });
        return document;
    }

}
