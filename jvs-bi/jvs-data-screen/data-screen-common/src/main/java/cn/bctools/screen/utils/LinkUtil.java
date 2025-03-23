package cn.bctools.screen.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.security.MessageDigest;

/**
 * @author admin
 */
@Slf4j
public class LinkUtil {
    /**
     * 通过长链接置换短链接
     *
     * @param longLink 长链接
     * @return 短链接
     */
    @SneakyThrows
    public static String generateShortLink(String longLink) {
        // 使用 MD5 算法对长链接进行哈希加密
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(longLink.getBytes());

        // 将哈希结果转换为短链接（16 进制字符串）
        StringBuilder shortLinkBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int hashByte = hashBytes[i] & 0xFF;
            String hex = Integer.toHexString(hashByte);
            if (hex.length() == 1) {
                shortLinkBuilder.append('0');
            }
            shortLinkBuilder.append(hex.charAt(hex.length() - 1));
        }
        return shortLinkBuilder.toString();
    }

    @SneakyThrows
    public static String getDomainName(String url) {
        URL parsedUrl = new URL(url);
        String domain = parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
        //防止存在端口
        if (parsedUrl.getPort() != -1) {
            domain += ":" + parsedUrl.getPort();
        }
        return domain;
    }
}
