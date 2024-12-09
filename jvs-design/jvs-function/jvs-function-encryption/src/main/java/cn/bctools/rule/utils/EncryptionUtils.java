package cn.bctools.rule.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.rule.encryption.encryp.EncryptionDto;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpUtil;
import org.apache.logging.log4j.util.Base64Util;

/**
 * 统一的加解密处理
 * 需要支持国密 sm2 sm3 sm4
 * base64,MD5,AES,MD5
 *
 * @author guojing
 */
public class EncryptionUtils {

    static final Digester MD_5 = new Digester(DigestAlgorithm.MD5);
    static final String HTTP = "http";

    /**
     * 公共加密串对象
     *
     * @param
     * @return
     */
    public static String encrypt(EncryptionDto dto) {
        String obj = null;
        switch (dto.getMode()) {
            case BASE64:
                //如果是http开头的，则下载后进行转换
                if (dto.getText().startsWith(HTTP)) {
                    byte[] bytes = HttpUtil.downloadBytes(dto.getText());
                    obj = Base64.encode(bytes);
                } else {
                    obj = Base64.encode(dto.getText().getBytes());
                }
                break;
            case AES:
                String aesKey = dto.getAesKey();
                byte[] decode = Base64.decode(aesKey.getBytes());
                AES aes = SecureUtil.aes(decode);
                return aes.encryptBase64(dto.getText());
            case MD5:
                obj = MD_5.digestHex(dto.getText());
                break;
            case SM2:
                SM2 sm2 = SmUtil.sm2(dto.getPrivateKey(), dto.getPublicKey());
                //加密
                obj = sm2.encryptBase64(dto.getText(), KeyType.PublicKey);
                break;
            default:
        }
        return obj;
    }

    /**
     * 公共解密
     *
     * @param dto
     * @return
     */
    public static String decode(EncryptionDto dto) {
        String obj = null;
        switch (dto.getMode()) {
            case BASE64:
                //如果是http开头的，则下载后进行转换
                obj = Base64.decodeStr(dto.getText());
                break;
            case AES:
                String aesKey = dto.getAesKey();
                byte[] decode = Base64.decode(aesKey.getBytes());
                AES aes = SecureUtil.aes(decode);
                return aes.decryptStr(dto.getText());
            case MD5:
                throw new BusinessException("执行错误");
            case SM2:
                SM2 sm2 = SmUtil.sm2(dto.getPrivateKey(), dto.getPublicKey());
                obj = StrUtil.utf8Str(sm2.decryptFromBcd(dto.getText(), KeyType.PrivateKey));
                break;
            default:
        }
        return obj;
    }

}
