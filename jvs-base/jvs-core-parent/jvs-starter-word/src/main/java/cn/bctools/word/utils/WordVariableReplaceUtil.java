package cn.bctools.word.utils;

import cn.bctools.common.utils.JvsJsonPath;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.function.Get;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
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
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import javax.xml.bind.JAXBElement;
import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        List<String> fields = varsMap.entrySet().stream().filter(e -> ObjectNull.isNull(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        MainDocumentPart mainDocumentPart = mlPackage.getMainDocumentPart();
        processElements(mainDocumentPart.getContent(), fields, new Stack<String>());

        VariablePrepare.prepare(mlPackage);

        Docx4jClearUtil.cleanDocumentPart(mainDocumentPart);

        // 替换表格列表
        replaceTbl(mlPackage, varsMap);
        // 替换页眉页脚变量
        replaceHeaderFooterVariableText(mlPackage, varsMap);
        // 替换普通变量
        replaceVariableTextElement(mlPackage, varsMap);
    }

    /**
     * 替换页眉页脚普通变了
     *
     * @param mlPackage
     * @param varsMap
     */
    private static void replaceHeaderFooterVariableText(WordprocessingMLPackage mlPackage, Map<String, Object> varsMap) {
        HeaderFooterPolicy headerFooterPolicy = mlPackage.getDocumentModel().getSections().get(0).getHeaderFooterPolicy();
        List<Object> headers = getVariableTextElement(headerFooterPolicy.getDefaultHeader());
        List<Object> footers = getVariableTextElement(headerFooterPolicy.getDefaultFooter());
        List<Object> texts = Stream.of(headers, footers).filter(ObjectNull::isNotNull).flatMap(Collection::stream).collect(Collectors.toList());
        replaceVariableText(mlPackage, texts, varsMap);
    }

    /**
     * 替换普通变量
     *
     * @param mlPackage
     * @param varsMap
     */
    private static void replaceVariableTextElement(WordprocessingMLPackage mlPackage, Map<String, Object> varsMap) {
        List<Object> texts = getVariableTextElement(mlPackage.getMainDocumentPart());
        replaceVariableText(mlPackage, texts, varsMap);
    }

    /**
     * 替换变量
     *
     * @param mlPackage
     * @param texts
     * @param varsMap
     */
    private static void replaceVariableText(WordprocessingMLPackage mlPackage, List<Object> texts, Map<String, Object> varsMap) {
        if (ObjectNull.isNull(texts)) {
            return;
        }
        for (Object text : texts) {
            Text textElement = (Text) text;
            // 获取所有变量
            List<String> variables = ReUtil.findAllGroup0(VARIABLE_PATTERN, textElement.getValue());
            variables.forEach(variable -> {
                String variableDataType = getVariableDataType(variable);
                String variableKey = getPureKey(variable);
                String variableParam = getVariableParam(variable);
                Object dataValue = JvsJsonPath.read(varsMap, variableKey);
                if (dataValue instanceof Date) {
                    dataValue = DateUtil.formatDateTime((Date) dataValue);
                }
                replace(mlPackage, textElement, variableDataType, variableKey, variableParam, dataValue);
            });
        }
    }

    private static final Pattern START_PATTERN = Pattern.compile("\\{\\{#(\\w+)\\}\\}");
    private static final Pattern END_PATTERN = Pattern.compile("\\{\\{/(\\w+)\\}\\}");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    /**
     * 递归处理所有文档元素
     */
    private static void processElements(List<Object> elements, List<String> objects, Stack<String> keyQueue) {
        List<Object> toRemove = new ArrayList<>();
        Iterator<Object> iterator = elements.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement<?>) obj).getValue();
            }
            if (obj instanceof P) {
                processParagraph((P) obj, toRemove, objects, keyQueue);
                if (toRemove.contains(obj)) {
                    iterator.remove();
                }
            } else if (obj instanceof Tbl) {
                processTable((Tbl) obj, toRemove, objects, keyQueue);
                if (toRemove.contains(obj)) {
                    iterator.remove();
                }
            } else if (obj instanceof ContentAccessor) {
                // 递归处理子元素（如文本框等）
                processElements(((ContentAccessor) obj).getContent(), objects, keyQueue);
            } else {
            }
        }
        elements.removeAll(toRemove);
    }

    /**
     * 处理段落（含内联标记）
     *
     * @return
     */
    private static void processParagraph(P p, List<Object> toRemove, List<String> objects, Stack<String> keyQueue) {
        Matcher startMatcher = START_PATTERN.matcher(p.toString());
        Matcher endMatcher = END_PATTERN.matcher(p.toString());

        // 检测条件块开始
        if (startMatcher.find()) {
            String condition = startMatcher.group(1);
            boolean show = objects.contains(condition);
            if (show) {
                keyQueue.push(condition);
            }
            //同文字里面是否有结束
            if (endMatcher.find()) {
                String group = endMatcher.group(1);
                //相同的文字
                if (condition.equals(group)) {
                    //替换内容
                    boolean boo = false;
                    Iterator<Object> iterator = p.getContent().iterator();
                    while (iterator.hasNext()) {
                        Object o = iterator.next();
                        if (o instanceof R) {
                            JAXBElement o1 = (JAXBElement) ((R) o).getContent().get(0);
                            Text value = (Text) o1.getValue();
                            String value1 = value.getValue();
                            //表示开始，，这里就删除
                            if (value1.equals("{{#" + group + "}}")) {
                                boo = true;
                                iterator.remove();
                            } else if (boo) {
                                if (value1.equals("{{/" + group + "}}")) {
                                    boo = false;
                                    keyQueue.pop();
                                }
                                iterator.remove();
                            }
                        }
                    }
                }
            }
        } else if (endMatcher.find()) {
            // 检测条件块结束
            if (!keyQueue.isEmpty()) {
                String pop = keyQueue.get(0);
                String group = endMatcher.group(1);
                if (pop.equals(group)) {
                    keyQueue.pop();
                    // 清理结束标记
                    toRemove.add(p);
                }
            }
        }

        // 处理当前条件状态
        if (!keyQueue.isEmpty()) {
            toRemove.add(p);
        }
    }

    /**
     * 处理表格结构
     */
    private static void processTable(Tbl tbl, List<Object> toRemove, List<String> objects, Stack<String> keyQueue) {
        // 处理当前条件状态
        if (!keyQueue.isEmpty()) {
            toRemove.add(tbl);
            return;
        }
        for (Object rowObj : tbl.getContent()) {
            if (rowObj instanceof Tr) {
                Tr row = (Tr) rowObj;
                for (Object cellObj : row.getContent()) {
                    if (cellObj instanceof JAXBElement) {
                        Tc cell = (Tc) ((JAXBElement<?>) cellObj).getValue();
                        processElements(cell.getContent(), objects, keyQueue);
                    }
                }
            }
        }
    }

    /**
     * 替换占位符
     */
    private static void replacePlaceholders(P p, ArrayList<String> objects) {
        for (Object item : p.getContent()) {
            if (item instanceof R) {
                R run = (R) item;
                for (Object textObj : run.getContent()) {
                    if (textObj instanceof Text) {
                        Text text = (Text) textObj;
                        Matcher m = PLACEHOLDER_PATTERN.matcher(text.getValue());
                        if (m.find()) {
//                            String value = objects.getOrDefault(m.group(1), "").toString();
                            text.setValue(m.replaceAll(""));
                        }
                    }
                }
            }
        }
    }

    /**
     * 在段落中替换正则匹配内容
     */
    private static void replaceInParagraph(P p, Pattern pattern, String replacement) {
        for (Object item : p.getContent()) {
            if (item instanceof R) {
                R run = (R) item;
                for (Object textObj : run.getContent()) {
                    if (textObj instanceof Text) {
                        Text text = (Text) textObj;
                        text.setValue(pattern.matcher(text.getValue()).replaceAll(replacement));
                    }
                }
            }
        }
    }

    /**
     * 提取段落文本
     */
    private static String extractText(P p) {
        StringBuilder sb = new StringBuilder();
        for (Object item : p.getContent()) {
            if (item instanceof R) {
                R run = (R) item;
                for (Object textObj : run.getContent()) {
                    if (textObj instanceof Text) {
                        sb.append(((Text) textObj).getValue());
                    }
                }
            }
        }
        return sb.toString();
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
            // 得到变量行（不限制第一行是变量行，直到找到有变量为止）
            int dynamicTrIndex = tableKeyTrIndex + 1;
            Tr dynamicTr = null;
            List<Object> texts = null;
            boolean flag = true;
            while (flag) {
                if (dynamicTrIndex >= table.getContent().size()) {
                    flag = false;
                    continue;
                }
                dynamicTr = (Tr) table.getContent().get(dynamicTrIndex);
                texts = Tool.getAllElementFromObject(dynamicTr, Text.class);
                if (ObjectNull.isNull(texts)) {
                    dynamicTrIndex++;
                    continue;
                }
                boolean hasVariable = texts.stream().anyMatch(t -> {
                    Text text = (Text) t;
                    String prefix = PREFIX + LEFT_BRACE;
                    return text.getValue().contains(prefix);
                });
                if (hasVariable) {
                    flag = false;
                } else {
                    dynamicTrIndex++;
                }
            }
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
                    // 获取所有变量
                    List<String> variables = ReUtil.findAllGroup0(VARIABLE_PATTERN, textElement.getValue());
                    variables.forEach(variable -> {
                        String variableDataType = getVariableDataType(variable);
                        String variableKey = getPureKey(variable);
                        String variableParam = getVariableParam(variable);
                        Object data = dataMap.get(variableKey);
                        replace(mlPackage, textElement, variableDataType, variableKey, variableParam, data);
                    });
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
     * @param variableParam    变量属性
     * @param dataValue        变量值
     */
    private static void replace(WordprocessingMLPackage mlp, Text textElement, String variableDataType, String variableKey, String variableParam, Object dataValue) {
        // 替换为图片
        if (DataType.IMAGE.name().equals(variableDataType)) {
            WordImageUtil.ImgParam imgParam = parseImgParam(variableParam);
            List<R> imageRs = WordImageUtil.createImageRs(mlp, dataValue, imgParam);
            if (ObjectNull.isNull(imageRs)) {
                textElement.setValue(textElement.getValue().replace(buildVariableKey(DataType.IMAGE, variableKey, variableParam), " "));
                return;
            }
            WordImageUtil.replaceImage(textElement, imageRs);
            return;
        }
        // 替换为富文本
        if (DataType.HTML.name().equals(variableDataType)) {
            List<Object> o = importerXmlHtml(mlp, dataValue);
            if (ObjectNull.isNull(o)) {
                textElement.setValue(textElement.getValue().replace(buildVariableKey(DataType.HTML, variableKey, variableParam), " "));
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
        replaceData = replaceData.replaceAll("\\n", "\n");
        List<String> textArr = new ArrayList<>();
        for (String s : replaceData.split("\\\\n")) {
            for (String string : s.split("\n")) {
                textArr.add(string);
            }
        }
        if (textArr.size() > 1) {
            List<Object> rParentContent = ((R) textElement.getParent()).getContent();
            for (int i = 0; i < textArr.size(); i++) {
                String text = textArr.get(i);
                if (i == 0) {
                    textElement.setValue(textElement.getValue().replace(buildVariableKey(variableKey), text));
                } else {
                    Text t = new Text();
                    t.setValue(text);
                    rParentContent.add(t);
                }
                rParentContent.add(new Br());
            }
        } else {
            textElement.setValue(textElement.getValue().replace(buildVariableKey(variableKey), replaceData));
        }
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
        return buildVariableKey(dataType, key, null);
    }

    /**
     * 构造变量key
     *
     * @param dataType
     * @param key
     * @param keyParam
     * @return
     */
    public static String buildVariableKey(DataType dataType, String key, String keyParam) {
        String type = dataType == null ? "" : dataType.name() + TYPE_SYMBOL;
        String param = ObjectNull.isNull(keyParam) ? "" : "?" + keyParam;
        return PREFIX + LEFT_BRACE + type + key + param + RIGHT_BRACE;
    }


    /**
     * 得到纯粹的变量
     *
     * @param variable
     * @return 去除各种符号后的变量。 如：${IMAGE#name} 返回name
     */
    public static String getPureKey(String variable) {
        return getVariable(variable).split("\\?")[0];
    }

    /**
     * 得到变量参数
     *
     * @param variable 变量
     * @return 变量参数
     */
    static String getVariableParam(String variable) {
        String[] arr = getVariable(variable).split("\\?");
        return arr.length == 1 ? null : arr[1];
    }

    /**
     * 获取变量
     *
     * @param variable 变量key
     * @return 变量
     */
    private static String getVariable(String variable) {
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

    /**
     * 解析图片变量获取
     * <p>
     * 变量格式：字段?w=200&h=100
     * 完整变量格式：${IMAGE#字段?参数}
     * <p>
     * “?”后跟参数，参数格式同GET请求参数格式
     *
     * @param variableParam 图片变量
     * @return 图片自定义参数
     */
    public static WordImageUtil.ImgParam parseImgParam(String variableParam) {
        WordImageUtil.ImgParam imgParam = new WordImageUtil.ImgParam();
        if (ObjectNull.isNull(variableParam)) {
            return imgParam;
        }
        String[] paramArr = variableParam.split("&");
        for (String p : paramArr) {
            String[] pArr = p.split("=");
            if (pArr.length != 2) {
                continue;
            }
            String paramName = pArr[0];
            String paramValue = pArr[1];
            if (Get.name(WordImageUtil.ImgParam::getW).equals(paramName)) {
                if (NumberUtil.isInteger(paramValue)) {
                    imgParam.setW(Integer.parseInt(paramValue));
                }
            }
            if (Get.name(WordImageUtil.ImgParam::getH).equals(paramName)) {
                if (NumberUtil.isInteger(paramValue)) {
                    imgParam.setH(Integer.parseInt(paramValue));
                }
            }
        }
        return imgParam;
    }

    @Getter
    @Setter
    private static class TableKeyTr {
        private String tableKey;
        private Tr tr;
        private Integer trIndex;
    }
}
