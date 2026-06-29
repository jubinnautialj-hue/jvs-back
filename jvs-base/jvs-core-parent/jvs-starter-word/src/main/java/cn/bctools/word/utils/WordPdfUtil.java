package cn.bctools.word.utils;

import cn.bctools.common.exception.BusinessException;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.finders.RangeFinder;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.*;

import static org.docx4j.Docx4J.createFOSettings;

/**
 * Word-Pdf统一工具类。
 * 可直接将Word内容进行替换,  或将word恩为pdf文档。工具使用到了docx4j2pdf工具包。 可替换不同的模板
 * 所有的模板都将使用到Redis,  将Inputstram存放到Redis中, 和内存中进行计算,  需要使用到的时候再拿出来。
 * map中存储的key 和redis中存储的相同 都是带前缀的,返回的都是文件的MD5
 *
 * @author guojing
 */

@Slf4j
public class WordPdfUtil {
    public static Map<String, String> templateMap = new HashMap<>();

    @SneakyThrows
    public static WordprocessingMLPackage loadTemplate(byte[] bytes) {
        long l = System.currentTimeMillis();
        log.debug("准备加载流对象");
        byte[] data = bytes;
        WordprocessingMLPackage load = WordprocessingMLPackage.load(new ByteArrayInputStream(data));
        load.setFontMapper(FontUtil.fontMapper);
        log.debug("WordprocessingMLPackage对象加载完成,耗时:{}毫秒", (System.currentTimeMillis() - l));
        return load;
    }

    /**
     * 书签替换
     *
     * @param bytes
     * @param varsMap
     * @return
     */
    @SneakyThrows
    public static WordprocessingMLPackage template(byte[] bytes, Map<String, Object> varsMap) {
        WordprocessingMLPackage template = loadTemplate(bytes);
        if (ObjectUtils.isNotEmpty(varsMap)) {
            long l = System.currentTimeMillis();
            //采用书签的方式进行变量替换
            MainDocumentPart mainDocumentPart = template.getMainDocumentPart();
            replaceContentByBookmark(mainDocumentPart, varsMap, new ArrayList<>(varsMap.keySet()));
            log.debug("template加载完成,耗时:{}毫秒", (System.currentTimeMillis() - l));
        } else {
            log.info("变量map为空,无需变量替换");
        }
        return template;
    }

    public static WordprocessingMLPackage template(WordprocessingMLPackage template, Map<String, Object> varsMap) {
        if (ObjectUtils.isNotEmpty(varsMap)) {
            long l = System.currentTimeMillis();
            //采用书签的方式进行变量替换
            MainDocumentPart mainDocumentPart = template.getMainDocumentPart();
            replaceContentByBookmark(mainDocumentPart, varsMap, new ArrayList<>(varsMap.keySet()));
            log.debug("template加载完成,耗时:{}毫秒", (System.currentTimeMillis() - l));
        } else {
            log.info("变量map为空,无需变量替换");
        }
        return template;
    }


    /**
     * 转换为pdf格式
     * 将工具包转为pdf文件。自定义输出流对象
     *
     * @param outputStream 输出流
     * @param template     模板对象
     */
    @SneakyThrows
    public static void convertDocx2Pdf(WordprocessingMLPackage template, OutputStream outputStream) {
        long l = System.currentTimeMillis();
        //docx4j docx转pdf
        FOSettings foSettings = createFOSettings();
        foSettings.setWmlPackage(template);
        Docx4J.toFO(foSettings, outputStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
        log.debug("转pdf耗时 " + (System.currentTimeMillis() - l));
    }

    /**
     * 转换为docx格式
     * 将工具包转为pdf文件。自定义输出流对象
     *
     * @param outputStream 输出流
     * @param template     模板对象
     */
    @SneakyThrows
    public static void convertDocx2Docx(WordprocessingMLPackage template, OutputStream outputStream) {
        //docx4j docx转pdf
        FOSettings foSettings = createFOSettings();
        foSettings.setWmlPackage(template);
        //直接保存为word
        template.save(outputStream);
    }

    /**
     * 初始化模板到内存中。
     *
     * @param key    生成的模板 prefix+md5
     * @param encode 文件流。
     */
    @SneakyThrows
    protected static void putTemplateMap(String key, String encode) {
        log.info("准备加载文件到内存中,key:{}", key);
        long l = System.currentTimeMillis();
        templateMap.put(key, encode);
        long l1 = System.currentTimeMillis();
        log.debug("耗时:{}毫秒,内存中的模板对象个数:=>{}", l1 - l, templateMap.size());
    }

    /**
     * 采用书签的方式进行word 变量替换
     *
     * @param mainDocumentPart 文档包 对象
     * @param map              变量map
     * @param bmList           书签list
     */
    public static void replaceContentByBookmark(MainDocumentPart mainDocumentPart, Map<String, Object> map, List<String> bmList) {
        try {
            log.info("准备变量替换");
            long l = System.currentTimeMillis();
            // 提取正文
            Document doc = mainDocumentPart.getContents();
            Body body = doc.getBody();

            // 获取段落
            List<Object> paragraphs = body.getContent();

            // 提取书签并获取书签的游标
            RangeFinder rt = new RangeFinder("CTBookmark", "CTMarkupRange");
            new TraversalUtil(paragraphs, rt);
            // 遍历书签
            for (CTBookmark bm : rt.getStarts()) {
                // 替换文本内容
                for (String bmName : bmList) {
                    if (bm.getName().equals(bmName)) {
                        Tool.replaceText(
                                bm, map.get(bm.getName()));
                    }
                }
            }
            long l1 = System.currentTimeMillis();
            log.info("变量替换完成,耗时:{}毫秒", l1 - l);
        } catch (Exception e) {
            log.info("书签变量替换异常:{}", e.getMessage());
            throw new BusinessException("变量替换异常");
        }
    }


    /**
     * 创建表格
     *
     * @param mlPackage mlPackage
     * @param titleList 标题
     * @param dataList  数据
     */
    @SneakyThrows
    public static void addTable(WordprocessingMLPackage mlPackage, List<String> titleList, List<LinkedHashMap<String, Object>> dataList) {
        //处理下行数据,在渲染数据时,key无用
        ArrayList<LinkedHashMap<Integer, Object>> afterHandlerList = new ArrayList<>(dataList.size());
        for (LinkedHashMap<String, Object> dataRow : dataList) {
            LinkedHashMap<Integer, Object> afterHandlerData = new LinkedHashMap<>();
            int i = 0;
            for (String key : dataRow.keySet()) {
                afterHandlerData.put(i++, dataRow.get(key));
            }
            afterHandlerList.add(afterHandlerData);
        }
        //处理行列
        MainDocumentPart mainDocumentPart = mlPackage.getMainDocumentPart();
        int length = titleList.size();
        Tbl table = Docx4jUtil.createTable(mlPackage, dataList.size() + 1, length);
        Docx4jUtil.setTableCellMargin(table, "200", "100", "200", "100");
        List<Tr> tblAllTr = Docx4jUtil.getTblAllTr(table);
        //创建表格
        for (int i = 0; i < tblAllTr.size(); i++) {
            List<Tc> trAllCell = Docx4jUtil.getTrAllCell(tblAllTr.get(i));
            for (int j = 0; j < trAllCell.size(); j++) {
                //插入内容
                String content;
                //第一行插入表头
                if (i == 0) {
                    content = titleList.get(j);
                } else {
                    //插入数据
                    LinkedHashMap<Integer, Object> afterHandlerData = afterHandlerList.get(i - 1);
                    Object o = afterHandlerData.get(j);
                    content = o == null ? "" : o.toString();
                    content = contentAutoAdjust(mlPackage, content, length);
                }
                Tc tc = trAllCell.get(j);
                Docx4jUtil.setTcContent(tc, content, "黑体", "Times New Roman");
                TblWidth tblWidth = new TblWidth();
                tblWidth.setW(new BigInteger("250"));
                tc.setTcPr(new TcPr());
                tc.getTcPr().setTcW(tblWidth);
            }
        }
        mainDocumentPart.addObject(table);
    }

    public static boolean containsKey(Map map, String key) {
        boolean flag = false;
        for (Object o : map.keySet()) {
            if (o.toString().contains(key)) {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 内容超过指定长度换行
     *
     * @param content 内容
     * @return
     */
    @SneakyThrows
    public static String contentAutoAdjust(WordprocessingMLPackage wordPackage, String content, int colNum) {
        //获取文档的可用宽度 A4可写大约是9000,字体五号,能写大约42个汉字,84个数字,90个字母
        //即五号字体下: 1个字母对应100 单位
        //            1个汉字对应214 单位
        //            1个数字对应107 单位
        int writableWidth = Docx4jUtil.getWritableWidth(wordPackage);
        //列宽
        double colWidth = new Double(String.valueOf(writableWidth / colNum));
        double contentWidth = getContentWidth(content);
        //占据单元格80%的时候考虑换行
        return doBreakLine(contentWidth, colWidth, content, new StringBuilder());
    }

    /**
     * 内容超过指定长度换行,本地测试用
     *
     * @param content 内容
     * @return
     */
    @SneakyThrows
    public static String contentAutoAdjust(String content) {
        //获取文档的可用宽度 A4可写大约是9000,字体五号,能写大约42个汉字,84个数字,90个字母
        //即五号字体下: 1个字母对应100 单位
        //            1个汉字对应214 单位
        //            1个数字对应107 单位
        //列宽
        double colWidth = 1500;
        double contentWidth = getContentWidth(content);
        //占据单元格80%的时候考虑换行
        return doBreakLine(contentWidth, colWidth, content, new StringBuilder());
    }

    public static String doBreakLine(double contentWidthDouble, double colWidthDouble, String content, StringBuilder result) {
        double v1 = 0.8;
        if (contentWidthDouble / colWidthDouble <= v1) {
            return result.append(content).toString();
        }
        //占据单元格80%的时候考虑换行
        if (contentWidthDouble / colWidthDouble > v1) {
            //获取换行的字符下标
            double v = colWidthDouble * v1;
            int index = getContentWidth(content, v);
            String theSecondHalf = content.substring(index);
            String theFirstHalf = content.substring(0, index);
            result.append(theFirstHalf);
            double contentWidth = getContentWidth(theSecondHalf);
            if (contentWidthDouble / colWidthDouble > v1) {
                result.append("\n");
                doBreakLine(contentWidth, colWidthDouble, theSecondHalf, result);
            }
        }
        return result.toString();
    }


    public static double getContentWidth(String content) {
        if (StrUtil.isBlank(content)) {
            return 0;
        }
        double width = 0.0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (PinyinUtil.isChinese(c)) {
                width = width + 214;
            }
            if (NumberUtil.isInteger(Character.toString(c))) {
                width = width + 107;
            }
            width = width + 100;
        }
        return width;
    }

    /**
     * 获取指定宽度下的字符下标
     *
     * @param content
     * @param width
     * @return
     */
    public static int getContentWidth(String content, double width) {
        if (StrUtil.isBlank(content)) {
            return 0;
        }
        double charWidth = 0.0;
        for (int i = 0; i < content.length(); i++) {
            Character c = content.charAt(i);
            if (PinyinUtil.isChinese(c)) {
                charWidth = charWidth + 214;
            } else if (NumberUtil.isInteger(c.toString())) {
                charWidth = charWidth + 107;
            } else {
                charWidth = charWidth + 100;
            }
            if (charWidth > width) {
                return i - 1;
            }
        }
        return content.length();
    }

}
