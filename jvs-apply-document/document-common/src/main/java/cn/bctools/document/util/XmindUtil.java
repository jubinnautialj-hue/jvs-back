package cn.bctools.document.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class XmindUtil {

    public static String getContent(InputStream inputStream) {
        File file = FileUtil.file("test.zip");
        FileUtil.writeFromStream(inputStream, file);
        File unzip = ZipUtil.unzip(file);
        File[] files = unzip.listFiles();
        Optional<File> first = Arrays.asList(files).stream().filter(e -> "content.json".equals(e.getName())).findFirst();
        String content = "";
        if (first.isPresent()) {
            content = FileUtil.readUtf8String(first.get());
        }
        boolean delete = file.delete();
        boolean del = FileUtil.del(unzip);
        log.info("xmind文件获取内容后删除源文件是否成功{}，删除解压文件是否成功{}", delete, del);
        return content;
    }
}
