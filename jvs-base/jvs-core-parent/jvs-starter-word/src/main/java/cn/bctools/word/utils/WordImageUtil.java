package cn.bctools.word.utils;

import cn.bctools.common.utils.ObjectNull;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.util.Units;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuxiaokang
 * WORD图片工具
 */
public class WordImageUtil {

    /**
     * 图片链接地址字段
     */
    private static final String IMAGE_URL = "url";

    /**
     * 创建图片元素集合
     * <p>
     *   imageData图片数据支持格式：
     *   - 对象中包含url绝对路径
     *   - 对象数组中包含url绝对路径
     *   - url绝对路径
     *
     * @param mlPackage
     * @param imageData 图片数据
     * @return 包含图片元素的R元素集合
     */
    public static List<R> createImageRs(WordprocessingMLPackage mlPackage, Object imageData) {
       return createImageRs(mlPackage, imageData, new ImgParam());
    }

    /**
     * 创建图片元素集合
     * <p>
     *   imageData图片数据支持格式：
     *   - 对象中包含url绝对路径
     *   - 对象数组中包含url绝对路径
     *   - url绝对路径
     *
     * @param mlPackage
     * @param imageData 图片数据
     * @param imgParam  自定义图片参数
     * @return 包含图片元素的R元素集合
     */
    public static List<R> createImageRs(WordprocessingMLPackage mlPackage, Object imageData, ImgParam imgParam) {
        if (ObjectNull.isNull(imageData)) {
            return Collections.emptyList();
        }
        List<String> images = new ArrayList<>();
        if (imageData instanceof Collection) {
            images = JSON.parseArray(JSON.toJSONString(imageData), JSONObject.class)
                    .stream()
                    .map(data -> data.getString(IMAGE_URL))
                    .filter(ObjectNull::isNotNull)
                    .collect(Collectors.toList());
        }
        if (imageData instanceof Map) {
            images.add(JSON.parseObject(JSON.toJSONString(imageData), JSONObject.class).getString(IMAGE_URL));
        }
        if (imageData instanceof CharSequence) {
            images.add(imageData.toString());
        }
        List<R> imageRs = new ArrayList<>();
        for (String image : images) {
            if (ObjectNull.isNull(image)) {
                continue;
            }
            if (HttpUtil.isHttp(image) || HttpUtil.isHttps(image)) {
                byte[] imageBytes = getImageBytes(image);
                R r = WordImageUtil.newWidthAndHeightImageR(mlPackage, imageBytes, null, null, imgParam.getW(), imgParam.getH());
                imageRs.add(r);
            }
        }
        return imageRs;
    }

    /**
     * 获取图片
     *
     * @param imgUrl 图片地址
     * @return 原图片byte[]
     */
    private static byte[] getImageBytes(String imgUrl) {
        return HttpUtil.downloadBytes(imgUrl);
    }


    /**
     * 变量替换图片
     *
     * @param text    Text元素
     * @param imageRs 图片
     */
    @SneakyThrows
    public static void replaceImage(Text text, List<R> imageRs) {
        R r = (R) text.getParent();
        r.getContent().clear();
        if (CollectionUtils.isEmpty(imageRs)) {
            return;
        }
        P p = (P) r.getParent();
        p.getContent().addAll(imageRs);
    }

    /**
     * 变量替换图片
     *
     * @param texts    Text标签
     * @param variable 待替换的变量
     * @param imageRs  图片
     */
    @SneakyThrows
    public static void variableReplaceImage(List<Object> texts, String variable, List<R> imageRs) {
        for (Object text : texts) {
            Text content = (Text) text;
            if (content.getValue().equals(variable)) {
                R r = (R) content.getParent();
                r.getContent().clear();
                P p = (P) r.getParent();
                p.getContent().addAll(imageRs);
            }
        }
    }

    /**
     * 生成图片
     *
     * @param apackage
     * @param imageBytes
     * @return R
     */
    @SneakyThrows
    public static R newImageR(WordprocessingMLPackage apackage, byte[] imageBytes) {
        return newImageR(apackage, imageBytes, null, null);
    }

    /**
     * 生成图片
     *
     * @param wordmlpackage
     * @param imageBytes
     * @param filenameHint  任何文本，例如原始文件名
     * @param altText       类似HTML的alt文本
     * @return R
     */
    @SneakyThrows
    public static R newImageR(WordprocessingMLPackage wordmlpackage, byte[] imageBytes, String filenameHint, String altText) {
        int id1 = (int) ((Math.random() * 1000) * (Math.random() * 1000));
        int id2 = (int) ((Math.random() * 1000) * (Math.random() * 1000));
        return newImageR(wordmlpackage, imageBytes, filenameHint, altText, id1, id2);
    }

    /**
     * 生成图片
     *
     * @param wordmlpackage
     * @param imageBytes
     * @param filenameHint  任何文本，例如原始文件名
     * @param altText       类似HTML的alt文本
     * @param width  宽
     * @param height 高
     * @return R
     */
    @SneakyThrows
    public static R newWidthAndHeightImageR(WordprocessingMLPackage wordmlpackage, byte[] imageBytes, String filenameHint, String altText, Integer width, Integer height) {
        int id1 = (int) ((Math.random() * 1000) * (Math.random() * 1000));
        int id2 = (int) ((Math.random() * 1000) * (Math.random() * 1000));
        width = Optional.ofNullable(width).orElse(0);
        height = Optional.ofNullable(height).orElse(0);
        return newImageR(wordmlpackage, imageBytes, filenameHint, altText, id1, id2, width, height);
    }

    /**
     * 生成图片
     *
     * @param aPackage
     * @param imageBytes
     * @param filenameHint 任何文本，例如原始文件名
     * @param altText      类似HTML的alt文本
     * @param id1          文档中唯一的id
     * @param id2          文档中另一个唯一的id
     * @return R
     */
    @SneakyThrows
    public static R newImageR(WordprocessingMLPackage aPackage, byte[] imageBytes, String filenameHint, String altText, int id1, int id2) {
        return newImageR(aPackage, imageBytes, filenameHint, altText, id1, id2, 0, 0);
    }

    /**
     * 生成图片
     *
     * @param aPackage
     * @param imageBytes
     * @param filenameHint 任何文本，例如原始文件名
     * @param altText      类似HTML的alt文本
     * @param id1          文档中唯一的id
     * @param id2          文档中另一个唯一的id
     * @param width  宽
     * @param height 高
     * @return R
     */
    @SneakyThrows
    public static R newImageR(WordprocessingMLPackage aPackage, byte[] imageBytes, String filenameHint, String altText, int id1, int id2, int width, int height) {
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(aPackage, imageBytes);
        Inline inline = null;
        if (width > 0 && height > 0 ) {
            int widthEmu = Units.pixelToEMU(width);
            int heightEmu = Units.pixelToEMU(height);
            inline = imagePart.createImageInline(filenameHint, altText, id1, id2, widthEmu, heightEmu, false);
        } else {
            inline = imagePart.createImageInline(filenameHint, altText, id1, id2, false);
        }
        ObjectFactory factory = Context.getWmlObjectFactory();
        R run = factory.createR();
        Drawing drawing = factory.createDrawing();
        run.getContent().add(drawing);
        drawing.getAnchorOrInline().add(inline);
        return run;
    }



    /**
     * 图片参数
     */
    @Getter
    @Setter
    public static class ImgParam {
        /**
         * 宽
         */
        private Integer w=50;
        /**
         * 高
         */
        private Integer h=50;
    }
}
