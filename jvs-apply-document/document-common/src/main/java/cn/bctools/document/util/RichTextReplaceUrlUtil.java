package cn.bctools.document.util;

import cn.bctools.document.po.SourceFileDownTypePo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 * 富文本下载 前缀添加
 */
public class RichTextReplaceUrlUtil {

    /**
     * 添加前缀
     *
     * @param richTextContent html内容
     * @param prefix          需要添加的前缀
     */
    public static String addImagePrefix(String richTextContent, String prefix) {
        Document document = Jsoup.parse(richTextContent);
        Elements images = document.select("img[src]");

        for (Element image : images) {
            String imageSrc = image.attr("src");
            if (!imageSrc.startsWith("http")) {
                image.attr("src", prefix + imageSrc);
            }
        }

        return document.toString();
    }

    /**
     * 添加前缀
     *
     * @param richTextContent html内容
     */
    public static List<SourceFileDownTypePo> getFile(String richTextContent) {
        Document document = Jsoup.parse(richTextContent);
        Elements images = document.select("img[src]");
        List<SourceFileDownTypePo> list = new ArrayList<>();
        for (Element image : images) {
            String imageSrc = image.attr("src");
            SourceFileDownTypePo sourceFileDownTypePo = new SourceFileDownTypePo().setType(1)
                    .setUrl(imageSrc);
            list.add(sourceFileDownTypePo);
        }
        Elements video = document.select("video[src]");
        for (Element element : video) {
            String imageSrc = element.attr("src");
            SourceFileDownTypePo sourceFileDownTypePo = new SourceFileDownTypePo().setType(0)
                    .setUrl(imageSrc);
            list.add(sourceFileDownTypePo);
        }
        return list;
    }
}
