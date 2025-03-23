package cn.bctools.document.util;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;

@Slf4j
public class MarkDownUtil {

    public static String getContent(InputStream inputStream) {
        File file = FileUtil.file("test.md");
        FileUtil.writeFromStream(inputStream, file);
        String content = FileUtil.readUtf8String(file);
        boolean delete = file.delete();
        log.info("MarkDown文件获取内容后删除文件是否成功{}", delete);
        return content;
    }
}
