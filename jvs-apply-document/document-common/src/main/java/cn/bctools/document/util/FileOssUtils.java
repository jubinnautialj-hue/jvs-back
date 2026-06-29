package cn.bctools.document.util;

import cn.bctools.document.entity.DcLibrary;
import cn.bctools.document.entity.enums.DcLibraryTypeEnum;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;

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
            log.info("下载onlyoffice文件报错", e);
            return null;
        }
    }

    /**
     * 生成文件名
     *
     * @param dcLibrary 文档信息
     * @return 文件名
     */
    public static String getMultipartFileName(DcLibrary dcLibrary) {
        return getMultipartFileName(dcLibrary.getType());
    }

    /**
     * 生成文件名
     * <p>
     * 格式:
     * "{随机串}.{文档类型}"
     * 例:
     * "2db1eb767b634b1cb39bbc3299758461.document_html"
     *
     * @param type 文档类型
     * @return 文件名
     */
    public static String getMultipartFileName(DcLibraryTypeEnum type) {
        return IdUtil.fastSimpleUUID().concat(StringPool.DOT).concat(type.value);
    }

    @SneakyThrows
    public static InputStream getMultipartInputStream(String pathname, String content) {
        byte[] serialize = ObjectUtil.serialize(content);
        return new MockMultipartFile(pathname, pathname, "text/plain; charset=ISO-8859-1", serialize).getInputStream();
    }

}
