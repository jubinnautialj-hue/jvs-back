package cn.bctools.document.entity.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 知识库类型枚举
 *
 * @author guojing
 */
@Getter
@AllArgsConstructor
public enum DcLibraryTypeEnum {
    /**
     * 知识库
     */
    knowledge("knowledge", "知识库", false),
    /**
     * 目录
     **/
    directory("directory", "目录", false),
    /**
     * 富文本文档
     **/
    document_html("document_html", "富文本文档", true),
    /**
     * 表格文档
     */
    document_xlsx("document_xlsx", "表格文档", true),
    /**
     * 脑图文档
     */
    document_map("document_map", "脑图文档", true),
    /**
     * 流程文档
     */
    document_flow("document_flow", "流程文档", true),
    /**
     * 无法识别的文档
     */
    document_unrecognized("document_unrecognized", "无法识别的文档", true),
    /**
     * 上传的文件
     */
    document_upload("document_upload", "上传的文件", false),
    /**
     * 直接创建office word文档
     */
    office_doc("office_doc", "office-word文档", true),
    /**
     * 直接创建office cvs
     */
    office_csv("office_csv", "office-csv文档", true),
    /**
     * office excel文档
     */
    office_xlsx("office_xlsx", "office-excel文档", true),
    /**
     * office pdf文档
     */
    office_pdf("office_pdf", "office-pdf文档", true),
    /**
     * office ppt文档
     */
    office_ppt("office_ppt", "office-ppt文档", true),
    /**
     * url
     */
    url_address("url_address", "url_address地址", true),
    /**
     * markdown
     */
    md("md", "markdown文档", true),
    /**
     * url
     */
    xmind("xmind", "xmind文档", true),
    ;
    @EnumValue
    public final String value;
    public final String desc;
    public final Boolean isDoc;

    /**
     * 根据后缀名获取枚举类型
     */
    public static DcLibraryTypeEnum suffixToEnum(String name) {
        switch (name) {
            case "doc":
            case "docx":
                return DcLibraryTypeEnum.office_doc;
            case "pdf":
                return DcLibraryTypeEnum.office_pdf;
            case "xls":
            case "xlsx":
                return DcLibraryTypeEnum.office_xlsx;
            case "ppt":
            case "pptx":
                return DcLibraryTypeEnum.office_ppt;
            case "csv":
                return DcLibraryTypeEnum.office_csv;
            case "md":
                return DcLibraryTypeEnum.md;
            case "xmind":
                return DcLibraryTypeEnum.xmind;
            default:
                return DcLibraryTypeEnum.document_upload;
        }
    }

    /**
     * 判断是否走的onlyOffice
     */
    public static Boolean isOffice(DcLibraryTypeEnum dcLibraryTypeEnum) {
        return Arrays.asList(office_doc, office_pdf, office_ppt, office_xlsx, office_csv).contains(dcLibraryTypeEnum);
    }

    /**
     * 判断是否为文件或知识库
     */
    public static Boolean isFile(DcLibraryTypeEnum dcLibraryTypeEnum) {
        return !Arrays.asList(knowledge, directory).contains(dcLibraryTypeEnum);
    }
}
