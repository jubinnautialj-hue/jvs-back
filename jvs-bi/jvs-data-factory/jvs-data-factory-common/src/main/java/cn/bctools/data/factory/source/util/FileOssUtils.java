package cn.bctools.data.factory.source.util;

import cn.bctools.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Administrator
 */
@Slf4j
public class FileOssUtils {

    /**
     * 获取文件流
     */
    public static InputStream getInputStream(String url) {
        //文件保存位置
        try {
            URL fileUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            //设置超时间为5秒
            conn.setConnectTimeout(5 * 1000);
            //得到输入流
            InputStream inputStream = conn.getInputStream();
            byte[] buffer = new byte[4 * 1024];
            int len;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.close();
            InputStream byteArrayInputStream = new ByteArrayInputStream(bos.toByteArray());
            inputStream.close();
            return byteArrayInputStream;
        } catch (Exception e) {
            log.info("下载文件报错", e);
            throw new BusinessException("获取文件错误");
        }
    }
}
