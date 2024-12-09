package cn.bctools.word.utils;

import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.hutool.core.util.ReUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.FormattingOption;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import javax.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: WORD变量替换
 * <p>
 * - 表格格式
 * -需要在表格中输入##表格key##
 * -在表格key所在行的下一行写列表变量${列表项}（不支持富文本）
 * - 变量格式
 * - ${变量key} ：普通变量
 * -${类型#变量key} ： 带类型的变量（如：${IMAGE#photo}）
 */
public class WordVariableReplaceUtil {


    /**
     * 数据类型
     */
    public enum DataType {
        /**
         * 文本
         */
        TEXT,
        /**
         * 表格
         */
        TABLE,
        /**
         * 网页
         */
        HTML,
        /**
         * 图片
         */
        IMAGE
    }

    /**
     * 表格字段KEY符号
     */
    public static final String TABLE_KEY_SYMBOL = "##";


    /**
     * 字段类型分割符
     */
    public static final String TYPE_SYMBOL = "#";

    /**
     * start符号
     */
    public static final String PREFIX = "$";

    /**
     * 中包含
     */
    public static final String LEFT_BRACE = "{";

    /**
     * 结尾
     */
    public static final String RIGHT_BRACE = "}";

    /**
     * 变量正则匹配
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * 表格变量字段正则匹配
     */
    private static final Pattern VARIABLE_TABLE_PATTERN = Pattern.compile("##(.*?)##");

    /**
     * 字段数据替换
     *
     * @param bytes   模板
     * @param varsMap Map<数据类型, 数据>
     * @return
     */
    @SneakyThrows
    public static WordprocessingMLPackage template(byte[] bytes, Map<String, Object> varsMap) {
        WordprocessingMLPackage template = WordPdfUtil.loadTemplate(bytes);
        // 替换数据
        replaceData(template, varsMap);
        return template;
    }

    @SneakyThrows
    public static WordprocessingMLPackage template(WordprocessingMLPackage template, Map<String, Object> varsMap) {
        // 替换数据
        replaceData(template, varsMap);
        return template;
    }

    /**
     * 替换数据
     *
     * @param mlPackage
     * @param varsMap   变量数据
     */
    @SneakyThrows
    private static void replaceData(WordprocessingMLPackage mlPackage, Map<String, Object> varsMap) {
        if (MapUtils.isEmpty(varsMap)) {
            return;
        }
        MainDocumentPart mainDocumentPart = mlPackage.getMainDocumentPart();
        VariablePrepare.prepare(mlPackage);
        Docx4jClearUtil.cleanDocumentPart(mainDocumentPart);

        // 替换表格列表
        replaceTbl(mlPackage, varsMap);
        // 替换普通变量
        replaceVariableTextElement(mlPackage, varsMap);
    }


    /**
     * 替换普通变量
     *
     * @param mlPackage
     * @param varsMap
     */
    private static void replaceVariableTextElement(WordprocessingMLPackage mlPackage, Map<String, Object> varsMap) {
        List<Object> texts = getVariableTextElement(mlPackage.getMainDocumentPart());
        for (Object text : texts) {
            Text textElement = (Text) text;
            // 获取所有变量
            List<String> variables = ReUtil.findAllGroup0(VARIABLE_PATTERN, textElement.getValue());
            variables.forEach(variable -> {
                String variableDataType = getVariableDataType(variable);
                String variableKey = getPureKey(variable);
                Object dataValue = JvsJsonPath.read(varsMap, variableKey);
                replace(mlPackage, textElement, variableDataType, variableKey, dataValue);
            });

        }
    }

    /**
     * 替换Tbl标签变量
     *
     * @param mlPackage 包
     * @param varsMap   数据
     * @return 替换后的标签
     */
    private static void replaceTbl(WordprocessingMLPackage mlPackage, Map<String, Object> varsMap) {
        for (Object elementObj : mlPackage.getMainDocumentPart().getContent()) {
            if (elementObj instanceof JAXBElement) {
                JAXBElement jaxbElement = (JAXBElement) elementObj;
                if (jaxbElement.getValue() instanceof Tbl) {
                    Tbl table = (Tbl) jaxbElement.getValue();
                    List<Object> tableContent = table.getContent();
                    // 替换表格信息
                    replaceTableData(table, mlPackage, tableContent, varsMap);
                }
            }
        }
    }

    /**
     * 替换表格中有表格key（##表格key##）的数据
     *
     * @param table        表格
     * @param mlPackage
     * @param tableContent 表格的行元素集合
     * @param varsMap      变量
     */
    @SneakyThrows
    private static void replaceTableData(Tbl table, WordprocessingMLPackage mlPackage, List<Object> tableContent, Map<String, Object> varsMap) {
        // 得到待填充列表数据的行信息
        List<TableKeyTr> tableKeyTrList = new ArrayList<>();
        for (int i = 0; i < tableContent.size(); i++) {
            TableKeyTr tableKeyTr = getTableKey((Tr) tableContent.get(i), i);
            if (ObjectNull.isNull(tableKeyTr)) {
                continue;
            }
            tableKeyTrList.add(tableKeyTr);
        }
        if (ObjectNull.isNull(tableKeyTrList)) {
            // 未找到需要填充的表格配置
            return;
        }

        // 替换所有表数据
        int offset = 0;
        for (TableKeyTr tableKeyTr : tableKeyTrList) {
            int tableKeyTrIndex = tableKeyTr.getTrIndex() + offset;
            table.getContent().set(tableKeyTrIndex, tableKeyTr.getTr());
            // 得到表格数据
            List<Map<String, Object>> tableDataList = (List) JvsJsonPath.read(varsMap, tableKeyTr.getTableKey());
            if (CollectionUtils.isEmpty(tableDataList)) {
                continue;
            }
            // 得到变量行(约定表格key所在行的下一行是变量行)
            int dynamicTrIndex = tableKeyTrIndex + 1;
            Tr dynamicTr = (Tr) table.getContent().get(dynamicTrIndex);
            List<Object> texts = Tool.getAllElementFromObject(dynamicTr, Text.class);
            if (CollectionUtils.isEmpty(texts)) {
                continue;
            }
            String dynamicTrXml = XmlUtils.marshaltoString(dynamicTr);
            // 解析后的变量 key-模板变量，List[0]-类型，List[1]-变量key
            Map<String, List<String>> variableMap = new HashMap<>(8);
            for (Object text : texts) {
                Text content = (Text) text;
                String variableDataType = getVariableDataType(content.getValue());
                String pureKey = getPureKey(content.getValue());
                // 若变量key是路径格式"如a.b.c"，直接取列表数据变量名
                int lastIndexOf = pureKey.lastIndexOf(".") + 1;
                String variableKey = pureKey.substring(lastIndexOf);
                variableMap.put(content.getValue(), Arrays.asList(variableDataType, variableKey));
            }
            // 遍历数据填充表格
            List<Object> newTableDataTr = new ArrayList<>();
            for (Map<String, Object> dataMap : tableDataList) {
                Tr newTr = (Tr) XmlUtils.unmarshalString(dynamicTrXml);
                List<Object> newTexts = getVariableTextElement(newTr);
                for (Object text : newTexts) {
                    Text textElement = (Text) text;
                    List<String> vs = variableMap.get(textElement.getValue());
                    if (CollectionUtils.isEmpty(vs)) {
                        continue;
                    }
                    String variableDataType = vs.get(0);
                    String variableKey = vs.get(1);
                    Object data = dataMap.get(variableKey);
                    replace(mlPackage, textElement, variableDataType, getPureKey(textElement.getValue()), data);
                }
                newTableDataTr.add(newTr);
            }
            table.getContent().remove(dynamicTrIndex);
            table.getContent().addAll(dynamicTrIndex, newTableDataTr);
            offset += newTableDataTr.size() - 1;
        }
    }

    /**
     * 替换变量
     *
     * @param mlp              包
     * @param textElement      Text节点
     * @param variableDataType 变量类型
     * @param variableKey      变量key（纯粹的变量名，如：name）
     * @param dataValue        变量值
     */
    private static void replace(WordprocessingMLPackage mlp, Text textElement, String variableDataType, String variableKey, Object dataValue) {
        // 替换为图片
        if (DataType.IMAGE.name().equals(variableDataType)) {
            List<R> imageRs = WordImageUtil.createImageRs(mlp, dataValue);
            if (ObjectNull.isNull(imageRs)) {
                textElement.setValue(textElement.getValue().replace(buildVariableKey(DataType.IMAGE, variableKey), " "));
                return;
            }
            WordImageUtil.replaceImage(textElement, imageRs);
            return;
        }
        // 替换为富文本
        if (DataType.HTML.name().equals(variableDataType)) {
            List<Object> o = importerXmlHtml(mlp, dataValue);
            if (ObjectNull.isNull(o)) {
                textElement.setValue(textElement.getValue().replace(buildVariableKey(DataType.HTML, variableKey), " "));
                return;
            }
            R r = (R) textElement.getParent();
            P p = (P) r.getParent();
            Object pParent = p.getParent();
            if (pParent instanceof Body) {
                MainDocumentPart documentPart = mlp.getMainDocumentPart();
                int pIndex = documentPart.getContent().indexOf(p);
                documentPart.getContent().remove(pIndex);
                documentPart.getContent().addAll(pIndex, o);
            }
            if (pParent instanceof Tc) {
                Tc tc = (Tc) pParent;
                tc.getContent().clear();
                tc.getContent().addAll(o);
            }
            return;
        }

        // 替换为文本
        String replaceData = ObjectNull.isNull(dataValue) ? " " : String.valueOf(dataValue);
        textElement.setValue(textElement.getValue().replace(buildVariableKey(variableKey), replaceData));
    }

    /**
     * 富文本转word元素
     * <p>
     * 支持：
     * - 表格变量替换为富文本
     * - 纯文本变量（表格外）替换为富文本
     *
     * @param htmlContent 富文本内容
     * @return 富文本转换后的word元素
     */
    @SneakyThrows
    private static List<Object> importerXmlHtml(WordprocessingMLPackage mlPackage, Object htmlContent) {
        if (ObjectNull.isNull(htmlContent)) {
            return Collections.emptyList();
        }
        String html = dataToXmlHtml(htmlContent);
        XHTMLImporterImpl importer = new XHTMLImporterImpl(mlPackage);
        importer.setHyperlinkStyle("Hyperlink");
        importer.setTableFormatting(FormattingOption.CLASS_TO_STYLE_ONLY);
        List<Object> o = importer.convert(html, null);
        for (Object obj : o) {
            // 获取text类型，设置字体
            List<Object> texts = Tool.getAllElementFromObject(obj, Text.class);
            for (Object text : texts) {
                Text t = (Text) text;
                R r = (R) t.getParent();
                RFonts fonts = new RFonts();
                fonts.setAscii("宋体");
                fonts.setHAnsi("宋体");
                fonts.setEastAsia("宋体");
                fonts.setCs("宋体");
                r.getRPr().setRFonts(fonts);
            }
            // 若是表格，设置表格宽度自适应
            if (obj instanceof Tbl) {
                Tbl tbl = (Tbl) obj;
                tbl.setTblGrid(new TblGrid());
                List<Object> tcObjs = Tool.getAllElementFromObject(tbl, Tc.class);
                for (Object tcObj : tcObjs) {
                    Tc tc = (Tc) tcObj;
                    TblWidth tblWidth = tc.getTcPr().getTcW();
                    tblWidth.setW(new BigInteger("0"));
                    tblWidth.setType(TblWidth.TYPE_AUTO);
                }
            }
            // 不支持<hr/>转换的Pict。 直接将Pict转换为单实线分割线
            List<Object> picts = Tool.getAllElementFromObject(obj, Pict.class);
            for (Object pictObj : picts) {
                Pict pict = (Pict) pictObj;
                R r = (R) pict.getParent();
                P p = (P) r.getParent();
                PPr pPr = new PPr();
                PPrBase.PBdr pBdr = new PPrBase.PBdr();
                CTBorder ctBorder = new CTBorder();
                ctBorder.setVal(STBorder.SINGLE);
                ctBorder.setColor("auto");
                ctBorder.setSz(new BigInteger("4"));
                ctBorder.setSpace(new BigInteger("0"));
                pBdr.setBottom(ctBorder);
                pPr.setPBdr(pBdr);
                p.setPPr(pPr);
                p.getContent().clear();
            }
        }
        return o;
    }

    /**
     * html数据转XHTML
     *
     * @param htmlContent html数据
     * @return 转换为xhtml后的字符串
     */
    private static String dataToXmlHtml(Object htmlContent) {
        Document document = Jsoup.parse((String) htmlContent);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml).escapeMode(Entities.EscapeMode.xhtml);
        Element meta = document.createElement("meta");
        meta.attr("charset", "utf-8");
        document.head().appendChild(meta);
        // 修改表格的broder='0'为1，否则插入到word没有表格线
        Elements tables = document.select("table");
        for (Element table : tables) {
            table.attr("border", "1");
        }
        // 图片url不能为空，且不能是相对路径
        Elements imgs = document.select("img");
        for (Element img : imgs) {
            String url = img.attr("src");
            if (ObjectNull.isNotNull(url) && (url.startsWith("http") || url.startsWith("data:image/"))) {
                continue;
            }
            img.remove();
        }
        return document.html();
    }

    /**
     * 得到表格key
     * ##表格key## 表示, 一行只能有一个表格key
     *
     * @return 表格名
     */
    @SneakyThrows
    private static TableKeyTr getTableKey(Tr dynamicTr, int index) {
        // 得到表格key
        String tableKey = "";
        for (Object o : dynamicTr.getContent()) {
            JAXBElement jaxbElement = (JAXBElement) o;
            Tc tc = (Tc) jaxbElement.getValue();
            String text = tc.getContent().stream().map(m -> {
                if (m instanceof P) {
                    return m.toString();
                }
                return "";
            }).collect(Collectors.joining());
            tableKey = ReUtil.getGroup1(VARIABLE_TABLE_PATTERN, text);
            if (ObjectNull.isNotNull(tableKey)) {
                // 移除表格key占位字符
                List<Object> texts = Tool.getAllElementFromObject(tc, Text.class);
                for (Object t : texts) {
                    Text txt = (Text) t;
                    txt.setValue(txt.getValue().replaceAll("##", "").replaceAll(tableKey, ""));
                }
                break;
            }
        }

        if (ObjectNull.isNull(tableKey)) {
            return null;
        }

        String str = XmlUtils.marshaltoString(dynamicTr);
        Tr newTr = (Tr) XmlUtils.unmarshalString(str.toString());
        TableKeyTr tableKeyTr = new TableKeyTr();
        tableKeyTr.setTableKey(tableKey);
        tableKeyTr.setTr(newTr);
        tableKeyTr.setTrIndex(index);
        return tableKeyTr;
    }

    /**
     * 获取有变量的text元素集合
     *
     * @param object
     * @return
     */
    private static List<Object> getVariableTextElement(Object object) {
        List<Object> texts = Tool.getAllElementFromObject(object, Text.class);
        if (CollectionUtils.isEmpty(texts)) {
            return Collections.emptyList();
        }
        String prefix = PREFIX + LEFT_BRACE;
        return texts.stream().filter(text -> {
            Text t = (Text) text;
            return t.getValue().contains(prefix);
        }).collect(Collectors.toList());
    }

    /**
     * 构造表格key
     *
     * @param key
     * @return
     */
    public static String buildTableKey(String key) {
        return TABLE_KEY_SYMBOL + key + TABLE_KEY_SYMBOL;
    }


    /**
     * 得到纯粹的表格变量
     *
     * @param key
     * @return 去除各种符号后的变量。 如：##tableName## 返回tableName
     */
    public static String getPureTableKey(String key) {
        return key.replace(TABLE_KEY_SYMBOL, "");
    }

    /**
     * 构造变量key
     *
     * @param key
     * @return
     */
    public static String buildVariableKey(String key) {
        return buildVariableKey(null, key);
    }

    /**
     * 构造变量key
     *
     * @param dataType
     * @param key
     * @return
     */
    public static String buildVariableKey(DataType dataType, String key) {
        String type = dataType == null ? "" : dataType.name() + TYPE_SYMBOL;
        return PREFIX + LEFT_BRACE + type + key + RIGHT_BRACE;
    }


    /**
     * 得到纯粹的变量
     *
     * @param variable
     * @return 去除各种符号后的变量。 如：${IMAGE#name} 返回name
     */
    public static String getPureKey(String variable) {
        String dataType = getVariableDataType(variable) + TYPE_SYMBOL;
        int startIndex = variable.indexOf(PREFIX + LEFT_BRACE, 0);
        if (startIndex == -1) {
            return "";
        }
        int endIndex = variable.indexOf(RIGHT_BRACE, startIndex + 2);
        if (endIndex == -1) {
            return "";
        }
        String variableKey = variable.substring(startIndex + 2, endIndex);
        return variableKey.replace(dataType, "");
    }

    /**
     * 得到变量的类型字符串
     *
     * @param variable
     * @return
     */
    private static String getVariableDataType(String variable) {
        int startIndex = variable.indexOf(PREFIX + LEFT_BRACE, 0);
        if (startIndex == -1) {
            return "";
        }
        int endIndex = variable.indexOf(TYPE_SYMBOL, startIndex + 2);
        if (endIndex == -1) {
            return "";
        }
        return variable.substring(startIndex + 2, endIndex);
    }

    @Getter
    @Setter
    private static class TableKeyTr {
        private String tableKey;
        private Tr tr;
        private Integer trIndex;
    }
}
