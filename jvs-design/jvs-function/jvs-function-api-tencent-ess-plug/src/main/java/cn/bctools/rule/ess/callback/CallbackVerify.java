package cn.bctools.rule.ess.callback;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author jvs
 * 回调消息验证签名
 */
@Slf4j

public class CallbackVerify {


    public static void main(String[] args) {

        try {
            // 回调消息体
            String payload = "**********";
            // secretToken 创建应用号时配置的
            String secretToken = "**********";

            // 1. 取出header [Content-Signature]
            String signFromHeader = "***********";
            // 2. 验证签名
            String hash = "sha256=" + hmacsha256(payload, secretToken);

            //3. 如果验证通过，继续处理。如果不通过，忽略该请求
//            log.info(hash.equals(signFromHeader));

        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public static String hmacsha256(String data, String key) throws Exception {
        Mac sha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec spec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256.init(spec);
        byte[] array = sha256.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

}

