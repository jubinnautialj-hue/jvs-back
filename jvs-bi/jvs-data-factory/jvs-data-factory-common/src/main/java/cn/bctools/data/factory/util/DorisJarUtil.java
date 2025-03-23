package cn.bctools.data.factory.util;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class DorisJarUtil {
    public static String jarUrlToMd5(String jarUrl) {
        try {
            URL url = new URL(jarUrl);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            String string = calculateMd5(inputStream);
            inputStream.close();
            return string;
        } catch (Exception e) {
            log.info("计算jar包的md5值错误:", e);
        }
        return null;
    }

    private static String calculateMd5(InputStream inputStream) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[1024];
        int numRead;

        try {
            // 读取流，但不超过buffer的大小
            numRead = inputStream.read(buffer);
            if (numRead > 0) {
                // 只更新实际读取到的字节
                md.update(buffer, 0, numRead);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read data", e);
        }
        byte[] bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(String.format("%02x", aByte & 0xff));
        }
        return sb.toString();
    }

}
