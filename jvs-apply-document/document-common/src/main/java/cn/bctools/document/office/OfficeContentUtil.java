package cn.bctools.document.office;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.dto.VariablesAndContentDto;
import cn.bctools.document.office.excel.ExcelReadUtil;
import cn.bctools.document.office.excel.ExcelVariablesReplaceUtil;
import cn.bctools.document.office.po.OfficeImagePo;
import cn.bctools.document.office.word.WordVariablesReplaceUtil;
import cn.bctools.document.util.MarkDownUtil;
import cn.bctools.document.util.OCRUtil;
import cn.bctools.oss.dto.BaseFile;
import cn.bctools.oss.template.OssTemplate;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.ClassUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ooxml.extractor.POIXMLTextExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class OfficeContentUtil {
    private static final String OFFICE_FILE_PATH = "office/";
    /**
     * 获取src资源
     */
    private static Pattern IMAGE_SRC_PATTERN = Pattern.compile("(src|SRC)=\"(.*?)\"");
    /**
     * 获取body中的内容
     */
    private static Pattern BODY_CONTENT_PATTERN = Pattern.compile("<body>(.*)</body>");
    /**
     * 提取文字包括字母数字
     */
    private static String DELETE_SYMBOL = "[^\\u4e00-\\u9fa50-9a-zA-Z]";

    /**
     * 公开的桶名称
     */
    private static String PUBLIC_BUCKET_NAME = "document-mgr";

    /**
     * 内容中的变量
     */
    private static String VARIABLE_PATTERN = "\\$\\{(.*?)\\}";

    /**
     * 变量数据替换
     *
     * @param inputStream 模板文件流
     * @param data        变量数据
     * @param suffix      后缀名
     */
    public static InputStream replace(InputStream inputStream, String suffix, Map<String, Object> data) {
        try {
            InputStream outStream;
            if ("docx".equals(suffix)) {
                outStream = WordVariablesReplaceUtil.searchAndReplace(inputStream, data);
            } else {
                outStream = ExcelVariablesReplaceUtil.searchAndReplace(inputStream, data);
            }
            return outStream;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取变量
     *
     * @param suffix      后缀名
     * @param inputStream 输入流
     * @param group       分组 0标识获取${xxx} 1标识获取 xxx
     */
    public static List<String> getVariables(String suffix, InputStream inputStream, int group) {
        String officeContent = OfficeContentUtil.getOfficeContent(suffix, inputStream, Boolean.TRUE);
        List<String> list = ReUtil.findAll(VARIABLE_PATTERN, officeContent, group);
        if (!list.isEmpty()) {
            list = list.parallelStream().distinct().collect(Collectors.toList());
        }
        return list;
    }

    /**
     * 获取变量和内容
     *
     * @param inputStream 输入流
     * @param suffix      后缀名
     * @param group       分组 0标识获取${xxx} 1标识获取 xxx
     */
    public static VariablesAndContentDto getVariablesAndContent(String suffix, InputStream inputStream, int group) {
        String officeContent = OfficeContentUtil.getOfficeContent(suffix, inputStream, Boolean.TRUE);
        List<String> list = ReUtil.findAll(VARIABLE_PATTERN, officeContent, group);
        if (!list.isEmpty()) {
            list = list.parallelStream().distinct().collect(Collectors.toList());
        }
        return new VariablesAndContentDto(officeContent.replaceAll(DELETE_SYMBOL, ""), list);
    }

    /**
     * 创建一个office 文件
     *
     * @param
     */
    @SneakyThrows
    public static BaseFile createOffice(String type) {
        String suffixName;
        switch (type) {
            case "doc":
            case "docx":
                suffixName = ".docx";
                break;
            case "pdf":
                suffixName = ".pdf";
                break;
            case "xls":
            case "xlsx":
                suffixName = ".xlsx";
                break;
            case "ppt":
            case "pptx":
                suffixName = ".pptx";
                break;
            case "csv":
                suffixName = ".csv";
                break;
            default:
                throw new BusinessException("类型错误!");
        }
        //读取模板文件
        ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        InputStream inputStream = defaultClassLoader.getResource(OFFICE_FILE_PATH + "1" + suffixName).openStream();
        String fileName = "新建文件" + suffixName;
        OssTemplate bean = SpringContextUtil.getBean(OssTemplate.class);
        BaseFile baseFile = bean.putFile(PUBLIC_BUCKET_NAME, SpringContextUtil.getApplicationContextName(), fileName, inputStream);
        inputStream.close();
        return baseFile;
    }


    /**
     * doc docx pdf获取内容
     *
     * @param file 文件数据
     */
    @SneakyThrows
    public static String getOfficeContent(MultipartFile file) {
        String suffix = FileUtil.getSuffix(file.getOriginalFilename());
        String content = getOfficeContent(suffix, file.getInputStream(), Boolean.FALSE);
        return content;
    }

    /**
     * doc docx pdf获取内容
     *
     * @param suffix     后缀名称
     * @param url        文件地址
     * @param isVariable 是否为变量 如果是变量获取内容直接返回不会对内容做任何变更 或者是否需要纯文本
     */
    @SneakyThrows
    public static String getOfficeContent(String suffix, String url, Boolean isVariable) {
        //判断是否需要读取内容
        if (Arrays.asList("doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx", "jpg", "jpeg", "png", "gif", "ico", "md").contains(suffix)) {
            return getOfficeContent(suffix, new URL(url).openStream(), isVariable);
        } else {
            return "";
        }
    }

    /**
     * doc docx pdf获取内容
     *
     * @param suffix      后缀名称
     * @param inputStream 文件流
     * @param isVariable  是否为变量 如果是变量获取内容直接返回不会对内容做任何变更 或者是否需要纯文本
     */
    @SneakyThrows
    public static String getOfficeContent(String suffix, InputStream inputStream, Boolean isVariable) {
        if (inputStream.available() == 0) {
            return "";
        }
        String content;
        switch (Objects.requireNonNull(suffix)) {
            case "doc":
                content = getDocContent(inputStream);
                break;
            case "docx":
                content = getDocxContent(inputStream);
                break;
            case "pdf":
                content = getPdfContent(inputStream);
                break;
            case "ppt":
                content = getPptContent(inputStream);
                break;
            case "pptx":
                content = getPptxContent(inputStream);
                break;
            case "xls":
            case "xlsx":
                content = ExcelReadUtil.readExcel(inputStream, suffix).stream().collect(Collectors.joining(" "));
                break;
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "ico":
                content = OCRUtil.getContent(inputStream, "1." + suffix, Boolean.TRUE);
                break;
            case "md":
                content = MarkDownUtil.getContent(inputStream);
                break;
            default:
                content = null;
        }
        if (java.util.Optional.ofNullable(content).isPresent() && !isVariable) {
            //获取文字
            content = content.replaceAll(DELETE_SYMBOL, "");
        }
        inputStream.close();
        return content;
    }

    /**
     * 获取word文件的内容
     *
     * @param inputStream 文件
     */
    public static String getDocContent(InputStream inputStream) {
        try (WordExtractor re = new WordExtractor(inputStream)) {
            return re.getText();
        } catch (Exception e) {
            log.info("获取word文档内容错误!", e);
            return null;
        }
    }

    /**
     * 获取docx文档内容
     *
     * @param inputStream 文件
     */
    public static String getDocxContent(InputStream inputStream) {
        try (OPCPackage opcPackage = OPCPackage.open(inputStream); POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage)) {
            return extractor.getText();
        } catch (Exception e) {
            log.info("获取word文档内容错误!", e);
            return null;
        }
    }

    /**
     * 获取ppt文件的内容 如果报错直接返回null
     *
     * @param inputStream 文件
     */
    public static String getPptContent(InputStream inputStream) {
        try (PowerPointExtractor extractor = new PowerPointExtractor(inputStream)) {
            return extractor.getText();
        } catch (Exception e) {
            log.info("获取ppt内容错误", e);
            return null;
        }
    }

    /**
     * 获取ppt文件的内容 如果报错直接返回null
     *
     * @param inputStream 文件
     */
    public static String getPptxContent(InputStream inputStream) {
        try (XMLSlideShow xss = new XMLSlideShow(inputStream)) {
            StringBuffer content = new StringBuffer();

            //得到幻灯片的集合，每一个XSLFSlide代表一页幻灯片的对象
            List<XSLFSlide> page = xss.getSlides();
            // 遍历每一张幻灯片
            for (XSLFSlide slide : page) {
                // 获得单张幻灯片内的各种对象 （包括但不限于文本框）
                // XSLFSlide.getShapes()得到XSLFShape对象，这个对象是单页幻灯片内所有对象的父类
                for (XSLFShape shape : slide.getShapes()) {
                    if (ObjectUtil.isNull(shape)) {
                        continue;
                    }
                    // 判断 当前对象是否为文本对象 XSLFTextShape
                    // 每一个XSLFTextShape代表一个文本框对象
                    if (shape instanceof XSLFTextShape) {
                        // 向下转型 XSLFTextShape 获得文本
                        String text = ((XSLFTextShape) shape).getText();
                        content.append(text);
                    }
                    // 判断 当前对象是否为表格对象 XSLFTable
                    //每一个XSLFTable代表一个表格对象
                    if (shape instanceof XSLFTable) {
                        // 向下转型为XSLFTable对象 一个表格对象
                        // 遍历行
                        for (XSLFTableRow row : ((XSLFTable) shape).getRows()) {
                            // 遍历单元格
                            for (XSLFTableCell cell : row.getCells()) {
                                // 获得文本
                                String text = cell.getText();
                                content.append(text);
                            }
                        }
                    }
                }
            }
            return content.toString();
        } catch (Exception e) {
            log.info("获取ppt内容错误", e);
            return null;
        }
    }

    /**
     * 获取word文件的内容 如果报错直接返回null
     *
     * @param inputStream 文件
     */
    public static String getPdfContent(InputStream inputStream) {
        StringBuilder result = new StringBuilder();
        try {
            PDFParser parser = new PDFParser(new RandomAccessBuffer(inputStream));
            parser.parse();
            PDDocument doc = parser.getPDDocument();
            PDFTextStripper textStripper = new PDFTextStripper();
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                textStripper.setStartPage(i);
                textStripper.setEndPage(i);
                //按顺序行读
                textStripper.setSortByPosition(true);
                String s = textStripper.getText(doc);
                //减少开支
                if (StrUtil.isNotBlank(s)) {
                    s = s.replaceAll(DELETE_SYMBOL, "");
                    if (StrUtil.isNotBlank(s)) {
                        result.append(s);
                    }
                }
            }
            doc.close();
            return result.toString();
        } catch (Exception e) {
            log.info("获取文件内容失败!");
            return null;
        }
    }

    /**
     * 获取html中的资源 并上传到文件服务器 替换资源路径
     *
     * @param content html内容
     */
    public static String matchImgSrcTag(String content) {
        //获取所有的img里面的内容 只需要 第二个正确表达式括号里面的内容
        List<String> imageList = ReUtil.findAll(IMAGE_SRC_PATTERN, content, 2);
        if (!imageList.isEmpty()) {
            OssTemplate bean = SpringContextUtil.getBean(OssTemplate.class);
            //资源上传
            List<OfficeImagePo> collect = imageList.parallelStream().distinct().map(e -> {
                File file = new File(e);
                BaseFile put = bean.put(PUBLIC_BUCKET_NAME, SpringContextUtil.getApplicationContextName(), FileUtil.getInputStream(file), file.getName(), Boolean.TRUE);
                String s = bean.fileLink(put.getFileName(), put.getBucketName());
                //删除原有的资源数据
                FileUtil.del(e);
                //注意这里如果是\ 需要转义后面才能进行替换
                e = e.replaceAll("\\\\", "\\\\\\\\");
                return new OfficeImagePo().setOriginalFilePath(e).setFileLink(s);
            }).collect(Collectors.toList());
            for (int i = 0; i < collect.size(); i++) {
                OfficeImagePo officeImagePo = collect.get(i);
                content = content.replaceAll(officeImagePo.getOriginalFilePath(), officeImagePo.getFileLink());
            }
        }
        return content;
    }

    /**
     * 获取body中的内容
     *
     * @param content html内容
     */
    public static String getHtmlBodyContent(String content) {
        return ReUtil.get(BODY_CONTENT_PATTERN, content, 1);
    }
}
