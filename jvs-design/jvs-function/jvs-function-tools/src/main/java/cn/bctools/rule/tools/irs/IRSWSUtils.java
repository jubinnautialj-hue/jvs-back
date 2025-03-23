package cn.bctools.rule.tools.irs;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.digest.SM3;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangguangpu
 * @version 1.0
 * @date 2024-07-11 17:27
 */
@Slf4j
public class IRSWSUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static Object postInvoiceModel(IRSInvoiceModelDto dto) {
        return postInvoiceModel(dto.getPrivateKeyStr(), dto.getSm4Key(), dto.getSm4IV(), dto.getNSRSBH(),
                dto.getSQBH(), dto.getSQWJ(), dto.getSQWJMC(), dto.getSQRRQ(),
                dto.getSQRRZ(), dto.getNSRMC(), dto.getTranId(), dto.getChannelId(),
                dto.getTranSeq(), dto.getTranDate(), dto.getTranTime(), dto.getVersion());
    }

    /**
     * 授权请求组装参数
     *
     * @param privateKeyStr 私钥
     * @param sm4Key        SM4key
     * @param sm4IV         SM4Iv
     * @param NSRSBH        纳税人识别号
     * @param SQBH          授权编号
     * @param SQWJ          授权文件地址
     * @param SQWJMC        授权文件名称
     * @param SQRRQ         授权日期起
     * @param SQRRZ         授权日期止
     * @param NSRMC         纳税人名称
     * @param tranId        调用服务名
     * @param channelId     渠道代码
     * @param tranSeq       32位uud不可重复
     * @param tranDate      当前日期格式yyyyMMdd如：20220101
     * @param tranTime      系统时间格式hhmmssSSS如161616001
     * @param version       当前服务版本号，默认传1.0
     * @return
     */

    public static String postInvoiceModel(String privateKeyStr, String sm4Key, String sm4IV, String NSRSBH,
                                          String SQBH, String SQWJ, String SQWJMC, String SQRRQ,
                                          String SQRRZ, String NSRMC, String tranId, String channelId,
                                          String tranSeq, String tranDate, String tranTime, String version) {

        String SQWJForBase64;
        try {
            SQWJForBase64 = getFileBase64(SQWJ);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("获取授权文件异常！");
        }
        String parma = "{" +
                "\"NSRMC\":" + "\"" + NSRMC + "\"," +
                "\"SQBH\":" + "\"" + SQBH + "\"," +
                "\"SQRRQ\":" + "\"" + SQRRQ + "\"," +
                "\"SQWJ\":" + "\"" + SQWJForBase64 + "\"," +
                "\"SQWJMC\":" + "\"" + SQWJMC + "\"," +
                "\"SQRRZ\":" + "\"" + SQRRZ + "\"," +
                "\"NSRSBH\":" + "\"" + NSRSBH + "\"" +
                "}";
        // 生成 签名
        String cipherParma = signature(parma, sm4Key, sm4IV, privateKeyStr);
        // 生成body
        String body = encryption(parma, sm4IV, sm4Key);
        // 生成请求参数
        String postMessage = assembleRequestParma(tranId, channelId, tranSeq, tranDate, tranTime, version,
                cipherParma, body);
        log.info("postMessage:   " + postMessage);
        return postMessage;
    }

    private static String getFileBase64(String SQWJ) throws IOException {
        InputStream inputStream = Files.newInputStream(new File(SQWJ).toPath());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        bufferedReader.close();
        return Base64.getEncoder().encodeToString(inputStream.toString().getBytes());
    }

    public static Object getInvoiceModel(IRSgetInvoiceModelDto dto) {
        return getInvoiceModel(dto.getPrivateKeyStr(), dto.getSm4Key(), dto.getSm4IV(), dto.getNSRSBH(), dto.getTranId(),
                dto.getChannelId(), dto.getTranSeq(), dto.getTranDate(), dto.getTranTime(), dto.getVersion());
    }

    /**
     * 数据请求 组装参数
     *
     * @param sm4Key        SM4key
     * @param sm4IV         SM4Iv
     * @param privateKeyStr 私钥
     * @param NSRSBH        纳税人识别号
     * @param tranId        调用服务名
     * @param channelId     渠道代码
     * @param tranSeq       32位uud不可重复
     * @param tranDate      当前日期格式yyyyMMdd如：20220101
     * @param tranTime      系统时间格式hhmmssSSS如161616001
     * @param version       前服务版本号，默认传1.0
     * @return
     */
    public static String getInvoiceModel(String privateKeyStr, String sm4Key, String sm4IV, String NSRSBH, String tranId,
                                         String channelId,
                                         String tranSeq, String tranDate, String tranTime, String version) {
        String parma = "{" +
                "\"NSRSBH\":" + "\"" + NSRSBH + "\"" +
                "}";
        // 生成 签名
        String cipherParma = signature(parma, sm4Key, sm4IV, privateKeyStr);
        // 生成body
        String body = encryption(parma, sm4IV, sm4Key);
        String postMessage = assembleRequestParma(tranId, channelId, tranSeq, tranDate, tranTime, version,
                cipherParma, body);
        log.info("postMessage = " + postMessage);
        return postMessage;
    }

    private static String assembleRequestParma(String tranId, String channelId, String tranSeq, String tranDate,
                                               String tranTime, String version, String cipherParma, String body) {
        return "{\n" +
                "\t\"head\": {\n" +
                "\t\t\"tranId\": " + "\"" + tranId + "\"" + ",\n" +
                "\t\t\"channelId\": " + "\"" + channelId + "\"" + ",\n" +
                "\t\t\"tranSeq\": " + "\"" + tranSeq + "\"" + ",\n" +
                "\t\t\"tranDate\": " + "\"" + tranDate + "\"" + ",\n" +
                "\t\t\"tranTime\": " + "\"" + tranTime + "\"" + ",\n" +
                "\t\t\"version\": " + "\"" + version + "\"" + ",\n" +
                "\t\t\"expands\": [{\n" +
                "\t\t\t\"name\": \"SIGNATURE\",\n" +
                "\t\t\t\"value\":" + "\"" + cipherParma + "\"" + "\n" +
                "\t\t}]\n" +
                "\t},\n" +
                "\t\"body\": " + "\"" + body + "\"" + "\n" +
                "}";
    }

    /**
     * 生成签名
     *
     * @param textStr       编码前报文
     * @param sm4KeyStr     SM4 key
     * @param IV            iv
     * @param privateKeyStr sm2 私钥
     * @return
     */
    private static String signature(String textStr, String sm4KeyStr, String IV, String privateKeyStr) {
        byte[] key64 = Base64.getDecoder().decode(sm4KeyStr.getBytes(StandardCharsets.UTF_8));
        byte[] iv64 = Base64.getDecoder().decode(IV.getBytes(StandardCharsets.UTF_8));
        byte[] bytes = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5PADDING", BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(key64, "SM4");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv64);
            cipher.init(Cipher.ENCRYPT_MODE, sm4Key, ivParameterSpec);
            bytes = cipher.doFinal(textStr.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String encrptStr = Base64.getEncoder().encodeToString(bytes);
        SM2 sm2 = SmUtil.sm2(privateKeyStr, null);
        SM3 sm3 = SmUtil.sm3();
        String s = sm3.digestHex(encrptStr);//把原文做 sm3 摘要System.out.printin("原文做 sm3 摘要:" + s);
        String sign = sm2.signHex(s);
        return sign;
    }
    //

    /**
     * SM4 加密
     *
     * @param text   需加密文本
     * @param keyStr sm4 key
     * @return
     */
    private static String encryption(String text, String iv, String keyStr) {
        byte[] key64 = Base64.getDecoder().decode(keyStr.getBytes(StandardCharsets.UTF_8));
        byte[] iv64 = Base64.getDecoder().decode(iv.getBytes(StandardCharsets.UTF_8));
        byte[] encode = text.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5PADDING", BouncyCastleProvider.PROVIDER_NAME);
            IvParameterSpec ips = new IvParameterSpec(iv64);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key64, "SM4"), ips);
            bytes = cipher.doFinal(encode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 解密
     *
     * @param key  密钥
     * @param iv   iv
     * @param data 需解密文本
     * @return
     */
    static String decryptor(String key, String iv, String body) {

        byte[] key64 = Base64.getDecoder().decode(key.getBytes(StandardCharsets.UTF_8));
        byte[] iv64 = Base64.getDecoder().decode(iv.getBytes(StandardCharsets.UTF_8));
        byte[] decode = Base64.getDecoder().decode(body);
        byte[] bytes = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5PADDING");
            IvParameterSpec ips = new IvParameterSpec(iv64);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key64, "SM4"), ips);
            bytes = cipher.doFinal(decode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 数据组装
     *
     * @param keys 需解密文本
     * @return
     */
    private static String dataAssembly(String[] keys, String val) {
        String[] values = val.split(",");
        // 定义键名数组，与values中的值一一对应
        // 使用HashMap存储键值对
        Map<String, String> dataMap = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            dataMap.put(keys[i], values[i]);
        }
        // 将Map转换为JSON对象
        JSONObject innerObject = new JSONObject(dataMap);
        // 将整个JSON对象包装在"YDJNDXXPJQK"键下
        JSONObject outerObject = new JSONObject();
        outerObject.set("YDJNDXXPJQK", innerObject);
        return outerObject.toString();
    }


//    public static static void main(String[] args) {
//        Security.addProvider(new BouncyCastleProvider());
//        String privateKeyStr = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgXofRuQzS7Z1MQklFpGSd3Q+edCrIM2r8" +
//                "/OVmHfxtAXqgCgYIKoEcz1UBgi2hRANCAAQGCclYDeQDTAVjs" +
//                "/LcsW6NiUEfC5NcWOvwtprFWC3fthP7AVjBZbnFi3QjaYqCzChnuT+6LrYalv4a1zeSSv0V";
//        String tranId = "CQSW.SFSJJHPT.SDSJJ.AUTHORIZATIONASK";
//        String tranIds = "CQSW.SFSJJHPT.SDSJJ.NSRFPMXCX";
//        String channelId = "CQZW.SDSJJ.DATASHARE";
//        String tranSeq = IdUtil.simpleUUID();
//        String tranDate = DateUtil.format(DateUtil.date(), "yyyyMMdd");
//        String tranTime = DateUtil.format(DateUtil.date(), "hhmmssSSS");
//        String version = "1.0";
//        String NSRSBH = "91500105MADGNJRT1P";
//        String NSRMC = "方莉";
//        String SQBH = "452G1Y018";
//        String SQRRQ = "2024-04-01";
//        String SQRRZ = "2024-08-01";
//        String SQWJ = "D:\\code\\xs\\demo\\src\\main\\resources\\META-INF\\企业授权书-重庆金言.pdf";
//        String SQWJMC = "企业授权书-重庆金言.pdf";
//        String sm4Key = "7pcKrVcOTU68F/emjnhomg==";
//        String sm4Iv = "OTAyMzE4NzIxMjM0MTQyMQ==";
//        BigDataAPI bigDataAPI = new BigDataAPI();
//  /*      // 授权生成 请求参数
//        bigDataAPI.postInvoiceModel(privateKeyStr, sm4Key, sm4Iv, NSRSBH, SQBH, SQWJ, SQWJMC, SQRRQ, SQRRZ, NSRMC, tranId,
//                channelId,
//                tranSeq, tranDate, tranTime, version);
//
//        //  数据请求请求参数
//        bigDataAPI.getInvoiceModel(privateKeyStr, sm4Key, sm4Iv, NSRSBH, tranIds,
//                channelId,
//                tranSeq, tranDate, tranTime, version);*/
//
//
///*        数据请求 返回 需解密body
//{
//            "gw-serial-id": "4xFk3KYB250Alc4kz//3k1J4g2fL6pPRQqWw0r/cdwsoTjqyQ1LdA04DobimOV0b1dgV6oA9jr/Fwg2FJc7VqL7qlyaHkx3dr4xqr2fHDwA0zV4WZRTKFvkOypYAlL71",
//                "body": "wh+sdc3D8bF15gZ7s7v3YG5ZIyO5dmhL8jazfJgwynuBfnDSqPBSBW9xTpdE1JRQaM0nvKugGKarlNn9+rj7oeSqbNWBwoO0zv/HvwHL/x84Y6L+dKru+J4XiK1ZNCZ4LB4gPXh1/xuG/s+ZKEJPpOp26zvUn8UHpwoXqPOq9oYT9Ih6xQBPVZdhFrrvA8u8zlgSwg8KwJ2AX9AGgmQKlXTDlFi0HGXGa93PMwj7U0wSOTUa/pK+SUmmxTblSD+nCu1iWwkA5N2sV5+p14jlAXqFOXixp2UIpG9XrEpsPyT2rN7uDjV6LSwVXxZjwbdmEkrEA3qPZDvdbsge3zLD2jkUmNSu7tlOh/Knov8A9Ck9Lj79BiT6WcMxz6OWYExYtTZJJ2szj5MLgfcNYAWq0Xnh/eByjwzs9yv8bzyGQ8I7w+JsSyjdFonk7actSFyQSydTyC0eztFfsWEpHZtt+TncoV7w6eZ2RHPL640+acdiqnMvZTOZ9v22fzEXFg97YUD2VtRwiZKgRaYNI8NXT9m/d2JUA6zoQ59d3J/wSp9cNtZ4v106ajO/eQQEEog1L7AQUusrDDxlgVOPsbAUIP5qGbHOKm89OW8SncTkjf+xZYUKltXnNrzkZkqzncV+ZFd8y3bAxfc8Gbuck9ObyUhDRlXN9AisXJZ/LtvN7KJ/qOmTJw4DUNQTXM05wCk4vfc7Idfe/jVkp78CmMeNcxdhJuCJ5CqF+xiea+wXvyGwuOIE73d5UFqWUU8xvhSGevCn+tdiAe3tmQuCHFwY3xqQtjFemqmcL2xE558z8hVPfG92d9voUSwRiYyfu/7nNgehVX2jwsxVhpVFShXNOXm+bIXS1WDkh0zzZTN1Copfo3E5qsleZjgWqhmWekb9xpoe+TaYIVbGjUG864bsy6/lEjDSNbe7KvVslgCAty4Snryh0ZMk9Et+rOB3dwLiETLEKBUbtTrRYBMG+kymZGifPPpTFnsfYGeFjS3uo/A8o5p/mx5L3+klTNsXO85IZCUZRFKO3JPydqV+7yxCaZhoWTrGy6uORhO1VQFY0ce8WsEQ9fF1PrJR2lrsnOJixAWWNG3/a2bCVkgCScgELi+cwpvVbmW0/arMiorr/mEpiZDG1v/yw/EiRBRafmKG09AgR4EVRFLgNtyzlQXujw==",
//                "head": {
//            "returnCode": "000000",
//                    "returnMsg": "处理成功",
//                    "expands": [
//            {
//                "value": "3045022100b533d2f379cb1616dcb34e40ae45eb83fe169901b5671028a964db04cb24bcae0220626dbb1b0f50c71afec15c0c77333d30154c3ace847ac4b96b7c5a3e1e40e113",
//                    "name": "SIGNATURE"
//            }
//		],
//            "tranId": "CQSW.SFSJJHPT.SDSJJ.NSRFPMXCX",
//                    "version": "1.0",
//                    "channelId": "CQZW.SDSJJ.DATASHARE",
//                    "tranSeq": "53728518cd974106be0027917ea7825c",
//                    "tranDate": "20240719",
//                    "tranTime": "1721353487362"
//        }
//        }*/
//        String DE = "{\n" +
//                "\t\"gw-serial-id\": \"Hljg4QlubnH64nPZGhJ6kLusBV40HuNWP01yj3MlQL9hF313ax0Zoy1eEK6BccxTkbIfKVPOrMYqHry5zTITMwp+m30rk7wVNu7dhRyLK+QSD8Ru28whuI8xR6oC32Ne\",\n" +
//                "\t\"head\": {\n" +
//                "\t\t\"tranDate\": \"20240723\",\n" +
//                "\t\t\"tranTime\": \"1721697260043\",\n" +
//                "\t\t\"returnCode\": \"000000\",\n" +
//                "\t\t\"returnMsg\": \"处理成功\",\n" +
//                "\t\t\"expands\": [\n" +
//                "\t\t\t{\n" +
//                "\t\t\t\t\"name\": \"SIGNATURE\",\n" +
//                "\t\t\t\t\"value\": \"3045022100c1f0b633f16244ab8f5315f8259b3a52a7b72480c5804a50c0d1785b5430534b022028abd59ade5706d30e66860a2c630045148ad95a5b8d6d063b7b18248852a776\"\n" +
//                "\t\t\t}\n" +
//                "\t\t],\n" +
//                "\t\t\"tranId\": \"CQSW.SFSJJHPT.SDSJJ.NSRFPMXCX\",\n" +
//                "\t\t\"channelId\": \"CQZW.SDSJJ.DATASHARE\",\n" +
//                "\t\t\"version\": \"1.0\",\n" +
//                "\t\t\"tranSeq\": \"912ab682d78045669af77d805fa56f87\"\n" +
//                "\t},\n" +
//                "\t\"body\": \"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\"\n" +
//                "}";
//
//        String decryptor = bigDataAPI.decryptor(sm4Key, sm4Iv, DE);
//        String s = bigDataAPI.returnData(decryptor);
//        System.out.println("s = " + s);
//    }

    /**
     * 传入解密后的body
     *
     * @return
     */
    public static JSONObject returnData(String decryptor) {
        String[] YDJNDXXPJQK = {"YXZS", "YXJEDJ", "ZZL", "ZFZS", "ZFJEDJ", "ZFZSZB", "CHZS", "CHJEDJ", "CHZSZB", "ZGZFFPJEDJ", "ZGCHFPJEDJ"};
        String[] YDJNDJXPJQK = {"YXZS", "YXJEDJ", "ZZL", "ZFZS", "ZFJEDJ", "ZFZSZB", "CHZS", "CHJEDJ", "CHZSZB", "ZGZFFPJEDJ", "ZGCHFPJEDJ"};
        String[] ZYSPFX = {"SPBM", "CS", "CSZB", "JEDJ", "JEZB"};
        String[] CGSPFX = {"SPBM", "CS", "CSZB", "JEDJ", "JEZB"};
        String[] KPJESDKHFX = {"GFMC", "GFSBH", "KPJEDJ", "JRZB"};
        String[] CGJESDKHFX = {"XFMC", "XFSBH", "CGJEDJ", "JRZB"};
        String[] XYKHJZD = {"DYJYEDJ", "JYEZB", "PMQSJYEDJ", "JEZB", "PMQSJYEDJ", "PMQSJYEPJZ", "XYKHZS"};
        String[] XYKHWDX = {"ZYKHBDS", "ZYKHBDSL"};
        String[] SYGYFJZD = {"DYJYEDJ", "JYEZB", "PMQSJYEDJ", "JEZB", "PMQSJYEDJ", "PMQSJYEPJZ", "SYKHZS"};
        String[] SYGYFWDX = {"ZYKHBDS", "ZYKHBDSL"};
        JSONObject jsonObjects = new JSONObject();
        JSONObject outerObject = new JSONObject();
        assemblyData(decryptor, "YDJNDXXPJQK", YDJNDXXPJQK, outerObject);
        assemblyData(decryptor, "YDJNDJXPJQK", YDJNDJXPJQK, outerObject);
        assemblyData(decryptor, "ZYSPFX", ZYSPFX, outerObject);
        assemblyData(decryptor, "CGSPFX", CGSPFX, outerObject);
        assemblyData(decryptor, "KPJESDKHFX", KPJESDKHFX, outerObject);
        assemblyData(decryptor, "CGJESDKHFX", CGJESDKHFX, outerObject);
        assemblyData(decryptor, "XYKHJZD", XYKHJZD, outerObject);
        assemblyData(decryptor, "XYKHWDX", XYKHWDX, outerObject);
        assemblyData(decryptor, "SYGYFJZD", SYGYFJZD, outerObject);
        assemblyData(decryptor, "SYGYFWDX", SYGYFWDX, outerObject);
        jsonObjects.putOnce("data", outerObject);
        return jsonObjects;
    }

    /**
     * 组装数据
     *
     * @return
     */
    public static JSONObject assemblyData(String json, String dms, String[] keys, JSONObject outerObject) {
        JSONArray jsonArray = JSONUtil.parseArray(json);
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        JSONArray rows = jsonObject.getJSONArray("rows");
        if (rows != null) {
            String val = rows.stream()
                    .filter(row -> row instanceof JSONObject)
                    .map(row -> (JSONObject) row)
                    .filter(row -> StrUtil.equals(row.getStr("dm"), dms))
                    .findFirst()
                    .map(row -> row.getStr("val"))
                    .orElse(null);
            String[] split = val.split(";");
            ArrayList<Object> objects = new ArrayList<>();
            for (String vl : split) {
                String[] values = vl.split(",");
                // 定义键名数组，与values中的值一一对应
                // 使用HashMap存储键值对
                Map<String, String> dataMap = new HashMap<>();
                for (int i = 0; i < keys.length; i++) {
                    if (ObjectUtil.isNotEmpty(val)){
                        dataMap.put(keys[i], values[i]);
                    }else {
                        dataMap.put(keys[i], "");
                    }
                }
                objects.add(dataMap);
            }
            // 将整个JSON对象包装在"YDJNDXXPJQK"键下
            outerObject.putOnce(dms, objects);
        }
        return outerObject;
    }

}
