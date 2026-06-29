package cn.bctools.wps.utils;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;


/**
 * @author xh
 */
@Slf4j
public class SignatureUtil {

    public static final String CONTENTTYPE = "application/json";
    public static final String SIGNATURE = "_w_signature";

    public static String getGmtDate() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        // 设置时区为GMT
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(cd.getTime());
    }

    public static String getMd5(Map<String, Object> paramMap) {
        try {
            String req = "";
            if (paramMap != null) {
                req = JSON.toJSONString(paramMap);
            }
            return DigestUtils.md5Hex(req);
        } catch (Exception e) {
            log.error("signatureUtil ", e);
        }
        return "";
    }

    public static String getKeyValueStr(Map<String, String> params) {
        List<String> keys = new ArrayList<String>() {
            {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    add(entry.getKey());
                }
            }
        };
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key) + "&";
            sb.append(key).append("=").append(value);
        }
        return sb.toString();
    }

    public static String getSignature(Map<String, String> params, String appSecret) {
        List<String> keys = new ArrayList<String>() {
            {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    add(entry.getKey());
                }
            }
        };

        // 将所有参数按key的升序排序
        keys.sort(String::compareTo);

        // 构造签名的源字符串
        StringBuilder contents = new StringBuilder();
        for (String key : keys) {
            if (SIGNATURE.equals(key)) {
                continue;
            }
            contents.append(key).append("=").append(params.get(key));
        }
        contents.append("_w_secretkey=").append(appSecret);
        // 进行hmac sha1 签名
        byte[] bytes = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, appSecret.getBytes()).hmac(contents.toString().getBytes());

        //字符串经过Base64编码
        String sign = encodeBase64String(bytes);
        try {
            return URLEncoder.encode(sign, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            log.error("encode error", e);
            return null;
        }

    }

    public static Map<String, String> paramToMap(String paramStr) {
        String[] params = paramStr.split("&");
        return new HashMap<String, String>(params.length) {
            {
                for (String param1 : params) {
                    String[] param = param1.split("=");
                    int i = 2;
                    if (param.length >= i) {
                        String key = param[0];
                        StringBuilder value = new StringBuilder(param[1]);
                        for (int j = i; j < param.length; j++) {
                            value.append("=").append(param[j]);
                        }
                        put(key, value.toString());
                    }
                }
            }
        };
    }

    /**
     * 生成签名
     *
     * @param action     GET、POST
     * @param url        调用接口的url，转换接口时传入接口地址不带参；查询接口时地址带参数
     * @param contentMd5 通过getMD5方法计算的值
     * @param headerDate 通过getGMTDate方法计算的值
     */
    private static String getSignature(String action, String url, String contentMd5, String headerDate, String convertAppsecret) {
        try {
            URL ur = new URL(url);
            String key = ur.getPath();
            if (!ObjectUtils.isEmpty(ur.getQuery())) {
                key = key + "?" + ur.getQuery();
            }
            String signStr = action + "\n" + contentMd5 + "\n" + CONTENTTYPE + "\n" + headerDate + "\n" + key;
            // 进行hmac sha1 签名
            byte[] bytes = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, convertAppsecret.getBytes()).hmac(signStr.getBytes());

            return encodeBase64String(bytes);
        } catch (Exception e) {
            log.error("encode error", e);
        }
        return "";
    }

    public static String getAuthorization(String action, String url, String contentMd5, String headerDate, String appid, String convertAppsecret) {
        //签名
        return "WPS " + appid + ":" + getSignature(action, url, contentMd5, headerDate, convertAppsecret);
    }

}
