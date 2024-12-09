package cn.bctools.word.utils;

import lombok.extern.slf4j.Slf4j;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.regex.Pattern;

/**
 * @author zhuxiaokang
 */
@Slf4j
public class Docx4jClearUtil {
    /**
     * 去任意XML标签 及 空白字符
     */
    private static final Pattern XML_PATTERN = Pattern.compile("<[^>]*>|\\s");

    private Docx4jClearUtil() {

    }

    /**
     * start符号
     */
    private static final char PREFIX = '$';

    /**
     * 中包含
     */
    private static final char LEFT_BRACE = '{';

    /**
     * 结尾
     */
    private static final char RIGHT_BRACE = '}';

    /**
     * 未开始
     */
    private static final int NONE_START = -1;

    /**
     * 未开始
     */
    private static final int NONE_START_INDEX = -1;

    /**
     * 开始
     */
    private static final int PREFIX_STATUS = 1;

    /**
     * 左括号
     */
    private static final int LEFT_BRACE_STATUS = 2;

    /**
     * 右括号
     */
    private static final int RIGHT_BRACE_STATUS = 3;

    /**
     * 清理文档接口方法,将被格式 分割的 替换变量 还原
     *
     * @param documentPart
     */
    public static boolean cleanDocumentPart(MainDocumentPart documentPart) throws Exception {
        if (documentPart == null) {
            return false;
        }
        Document document = documentPart.getContents();
        String wmlTemplate = XmlUtils.marshaltoString(document, true, false, Context.jc);
        document = (Document) XmlUtils.unwrap(Docx4jClearUtil.doCleanDocumentPart(wmlTemplate, Context.jc));
        documentPart.setContents(document);
        return true;
    }

    /**
     * doCleanDocumentPart
     *
     * @param wmlTemplate
     * @param jc
     * @return
     * @throws JAXBException
     */
    public static Object doCleanDocumentPart(String wmlTemplate, JAXBContext jc) throws JAXBException {
        log.info("======执行清除方法=====");
        // 进入变量块位置
        int curStatus = NONE_START;
        // 开始位置
        int keyStartIndex = NONE_START_INDEX;
        // 当前位置
        int curIndex = 0;
        char[] textCharacters = wmlTemplate.toCharArray();
        StringBuilder documentBuilder = new StringBuilder(textCharacters.length);
        documentBuilder.append(textCharacters);
        // 新文档
        StringBuilder newDocumentBuilder = new StringBuilder(textCharacters.length);
        // 最后一次写位置
        int lastWriteIndex = 0;
        for (char c : textCharacters) {
            switch (c) {
                case PREFIX:
                    // TODO 不管其何状态直接修改指针,这也意味着变量名称里面不能有PREFIX
                    keyStartIndex = curIndex;
                    curStatus = PREFIX_STATUS;
                    break;
                case LEFT_BRACE:
                    if (curStatus == PREFIX_STATUS) {
                        curStatus = LEFT_BRACE_STATUS;
                    }
                    break;
                case RIGHT_BRACE:
                    if (curStatus == LEFT_BRACE_STATUS) {
                        // 接上之前的字符
                        newDocumentBuilder.append(documentBuilder.substring(lastWriteIndex, keyStartIndex));
                        // 结束位置
                        int keyEndIndex = curIndex + 1;
                        // 替换
                        String rawKey = documentBuilder.substring(keyStartIndex, keyEndIndex);
                        log.info("-----需要替换的标签:"+rawKey);
                        // 干掉多余标签
                        String mappingKey = XML_PATTERN.matcher(rawKey).replaceAll("");
                        log.info("-----替换完成的字符:"+mappingKey);
                        newDocumentBuilder.append(mappingKey);
                        lastWriteIndex = keyEndIndex;

                        curStatus = NONE_START;
                        keyStartIndex = NONE_START_INDEX;
                    }
                default:
                    break;
            }
            curIndex++;
        }
        // 余部
        if (lastWriteIndex < documentBuilder.length()) {
            newDocumentBuilder.append(documentBuilder.substring(lastWriteIndex));
        }
        return XmlUtils.unmarshalString(newDocumentBuilder.toString(), jc);
    }
}
