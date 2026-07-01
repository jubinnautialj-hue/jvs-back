package cn.bctools.rule.encryption.encryp;

/**
 * 加解密类型声明
 *
 * @author guojing
 */
public enum EncryptionEnum {
    /**
     * SM2加密 {@linkplain https://blog.csdn.net/andylau00j/article/details/54427395}
     */
    SM2,
    /**
     * 加密 {@linkplain https://blog.csdn.net/andylau00j/article/details/54427395}
     */
    BASE64,
    /**
     * 加密 {@linkplain https://blog.csdn.net/andylau00j/article/details/54427395}
     */
    MD5,
    /**
     * 加密 {@linkplain https://blog.csdn.net/andylau00j/article/details/54427395}
     */
    AES;

}
