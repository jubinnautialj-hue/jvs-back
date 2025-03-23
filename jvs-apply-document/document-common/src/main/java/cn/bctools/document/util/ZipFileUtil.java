package cn.bctools.document.util;


import cn.bctools.document.po.UnzipPo;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ZipFileUtil {

    //文件解压流
    private static ZipArchiveInputStream getZipFile(File zipFile) throws Exception {
        return new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
    }


    /**
     * 获取解压后数据
     */
    public static List<UnzipPo> unzip(File file) {
        List<UnzipPo> list = new ArrayList<>();
        try (ZipArchiveInputStream inputStream = getZipFile(file)) {
            ZipArchiveEntry entry;
            while ((entry = inputStream.getNextZipEntry()) != null) {
                List<String> filePath = StrUtil.split(entry.getName(), "/",Boolean.TRUE,Boolean.TRUE);
                //文件或者文件夹名称
                String name = filePath.get(filePath.size() - 1);
                UnzipPo unzipPo = new UnzipPo()
                        .setFilePath("")
                        .setFilePath(entry.getName())
                        .setTopIs(Boolean.FALSE);
                //判断是否为顶级  如果路径参数只有一个就表示是顶级
                if (filePath.size() == BigDecimal.ROUND_DOWN) {
                    unzipPo.setTopIs(Boolean.TRUE);
                } else {
                    //设置文件路径 删除当前文件或者文件夹
                    filePath.remove(name);
                    //上级文件夹路径
                    unzipPo.setParentLevel(String.join("/", filePath)+"/");
                }
                unzipPo.setName(name)
                        .setIsDirectory(entry.isDirectory());
                if (!entry.isDirectory()) {
                    //获取流文件
                    ByteArrayOutputStream outputStream = cloneInputStream(inputStream);
                    if (outputStream != null) {
                        unzipPo.setInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
                        outputStream.close();
                    }
                }
                list.add(unzipPo);
            }
        } catch (Exception e) {
            log.info("文件解压错误", e);
        }
        return list;
    }

    private static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            log.info("压缩时获取流文件错误", e);
            return null;
        }
    }
}
