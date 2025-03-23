package cn.bctools.document.util;

import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.document.config.CommonConfig;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;

@Slf4j
public class OCRUtil {

    /**
     * 提取文字包括字母数字
     */
    private static String DELETE_SYMBOL = "[^\\u4e00-\\u9fa5]";


    /**
     * 获取内容
     *
     * @param inputStream 文件流
     * @param fileName    文件名称 用于生成文件
     * @param isGetText   是否获取纯文本
     */
    public static String getContent(InputStream inputStream, String fileName, boolean isGetText) {
        String ocrUrl = SpringContextUtil.getBean(CommonConfig.class).getOcrUrl();
        if (StrUtil.isBlank(ocrUrl)) {
            return null;
        }
        File file;
        try {
            file = FileUtil.writeFromStream(inputStream, FileUtil.file(fileName));
        } catch (Exception e) {
            log.error("ocr 通过url获取文件错误", e);
            return null;
        }
        HttpResponse execute = HttpUtil.createPost(ocrUrl).form("compress", 1600).form("file", file).execute();
        boolean delete = file.delete();
        log.info("ocr识别后删除文件:{}", delete);
        if (execute.getStatus() == 200) {
            JSONObject jsonObject = JSONObject.parseObject(execute.body());
            int code = jsonObject.getIntValue("code");
            if (code == 200) {
                String content = jsonObject.getJSONObject("data").getString("raw_out");
                if (StrUtil.isNotEmpty(content) && isGetText) {
                    return content.replaceAll(DELETE_SYMBOL, "");
                }
                return content;
            }
        }
        return null;
    }
}
