package cn.bctools.common.utils;

import cn.hutool.core.codec.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;


/**
 * @author jvs GZip工具类
 * zip压缩解压并使用Base64进行编码工具类
 * 调用：
 * 压缩
 * GZipUtil.compress(str)
 */
public class GzipUtil {

    /**
     * 将字符串进行gzip压缩
     *
     * @param data
     * @return
     */
    public static String compress(String data) {
        if (data == null || data.length() == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(data.getBytes("utf-8"));
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encode(out.toByteArray());
    }

}
