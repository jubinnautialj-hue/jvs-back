package cn.bctools.common.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.bctools.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <DecodePasswordUtil>
 * 为保证每一个应用使用自己的解密数据传递到后端，此功能统一加了一个使用appid ， base64默认填充为16位， 再进行处理
 *
 * @author Administrator
 */
@Slf4j
public class PasswordUtil {

    static Map<String, AES> appKeyMap = new HashMap<>(8);

    /**
     * 解密
     *
     * @param encodedPassword 密文
     * @return 明文
     */
    public static String decodedPassword(String encodedPassword) {
        return decodedPassword(encodedPassword, SpringContextUtil.getKey());
    }

    /**
     * 解密
     *
     * @param encodedPassword 密文
     * @param appId           前端应用的APPID
     * @return 明文
     */
    public static String decodedPassword(String encodedPassword, String appId) {
        try {
            AES aes = getKey(appId);
            return aes.decryptStr(encodedPassword, StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            log.error("解密密码不正确,{}", encodedPassword, appId);
            throw new BusinessException("解密失败");
        }
    }

    /**
     * 加密
     *
     * @param str 明文
     * @return 密文
     */
    public static String encodePassword(String str) {
        return encodePassword(str, SpringContextUtil.getKey());
    }

    /**
     * 加密
     *
     * @param str   明文
     * @param appId 前端应用的APPID
     * @return 密文
     */
    public static String encodePassword(String str, String appId) {
        try {
            AES aes = getKey(appId);
            return aes.encryptHex(str.trim(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密密码不正确,{}", str, appId);
            throw new BusinessException("解密失败");
        }
    }

    /**
     * 替换Key通过APPID获取前端加密key
     *
     * @param appId
     * @return
     */
    private static AES getKey(String appId) {
        if (appKeyMap.containsKey(appId)) {
            return appKeyMap.get(appId);
        }
        String key = Base64.encode(appId);
        //超过16位，截取前面 16位
        int i = 16;
        if (key.length() >= i) {
            key = key.substring(0, i);
        }
        String format = String.format("%016d", 0);
        key += format.substring(key.length());
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        AES aes = new AES(Mode.CBC, Padding.ZeroPadding, bytes, bytes);
        appKeyMap.put(appId, aes);
        return aes;
    }

    /**
     * 校验密码安全等级，如果密码简单直接报错
     */
    public static String checkPassword(String passwordStr) {
        String regexz = "\\d*";
        String regexs = "[a-zA-Z]+";
        String regext = "\\W+$";
        String regexzt = "\\D*";
        String regexst = "[\\d\\W]*";
        String regexzs = "\\w*";
        String regexzst = "[\\w\\W]*";

        if (passwordStr.matches(regexz)) {
            throw new BusinessException("密码过于简单请重新设置建议至少8位包含大小写");
        }
        if (passwordStr.matches(regexs)) {
            throw new BusinessException("密码过于简单请重新设置建议至少8位包含大小写");
        }
        if (passwordStr.matches(regext)) {
            throw new BusinessException("密码过于简单请重新设置建议至少8位包含大小写");
        }
        if (passwordStr.matches(regexzt)) {
            return "中";
        }
        if (passwordStr.matches(regexst)) {
            return "中";
        }
        if (passwordStr.matches(regexzs)) {
            return "中";
        }
        if (passwordStr.matches(regexzst)) {
            return "强";
        }
        return passwordStr;

    }

}
