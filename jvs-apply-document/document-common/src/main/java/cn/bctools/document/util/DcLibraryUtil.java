package cn.bctools.document.util;

import cn.bctools.document.constant.Constant;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;


/**
 * @author : GaoZeXi
 */
public class DcLibraryUtil {
    private static final String OFFICE_TO_HTML_FILE_NAME = "{}{} V{}";

    private DcLibraryUtil() {
    }

    /**
     * 获取随机key 用于存储redis 使用 文档id 作为层级,方便以后 对该key操作,列入 取消分享
     *
     * @return
     */
    public static String getKnowledgeLinkKey(String key) {
        return String.format(Constant.KNOWLEDGE_LINK_KEY, key);
    }

    /**
     * 文档名称
     */
    public static String getOfficeToHtmlFileName(String name, int count) {
        name += DateUtil.today();
        return StrUtil.format(OFFICE_TO_HTML_FILE_NAME, name, DateUtil.today(), count + 1);
    }
}
