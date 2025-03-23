package cn.bctools.rule.encryption.encryp;//package cn.bctools.rule.encryption.encryp;
//
//import cn.bctools.common.exception.BusinessException;
//import cn.bctools.common.utils.ObjectNull;
//import cn.bctools.rule.annotations.Rule;
//import cn.bctools.rule.entity.enums.ClassType;
//import cn.bctools.rule.entity.enums.RuleGroup;
//import cn.bctools.rule.function.BaseCustomFunctionInterface;
//import cn.bctools.rule.utils.EncryptionUtils;
//import cn.hutool.core.codec.Base64;
//import cn.hutool.crypto.SmUtil;
//import cn.hutool.crypto.asymmetric.SM2;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//
//import javax.crypto.KeyGenerator;
//import java.security.NoSuchAlgorithmException;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author guojing
// */
//@Slf4j
//@Rule(value = "数据加解密",
//        group = RuleGroup.加密插件,
//        test = true,
//        returnType = ClassType.文本,
//        order = 10,
////        iconUrl = "rule-hsmjiamifuwu",
//        explain = "提供了多种不一样的加密和解密方式并执行国密"
//)
//public class EncryptionServiceImpl implements BaseCustomFunctionInterface<EncryptionDto> {
//    /**
//     * 是否获取密钥：false 加解密、true 获取SM2、AES密钥；
//     */
//    private boolean flag = false;
//
//    /**
//     * 情况比较复杂需要自行校验
//     * 需要在实体对象上进行添加注解才能执行此方法{@linkplain cn.bctools.rule.annotations.Inspect}
//     *
//     * @param o
//     */
//    @SneakyThrows
//    @Override
//    public void inspect(EncryptionDto o) {
//        if (ObjectNull.isNull(o.getText())) {
//            flag = true;
//            return;
//        }
//        flag = false;
//        EncryptionEnum mode = o.getMode();
//        switch (mode) {
//            case SM2:
//                SM2 sm2 = SmUtil.sm2();
//                if (o.isType()) {
//                    //加密
//                    if (ObjectNull.isNull(o.getPublicKey())) {
//                        o.setPublicKey(sm2.getPublicKeyBase64());
//                        o.setPrivateKey(sm2.getPrivateKeyBase64());
//                    }
//                } else {
//                    //解密
//                    if (ObjectNull.isNull(o.getPrivateKey())) {
//                        o.setPublicKey(sm2.getPublicKeyBase64());
//                        o.setPrivateKey(sm2.getPrivateKeyBase64());
//                    }
//                }
//                break;
//            case AES:
//                if (o.isType()) {
//                    if (ObjectNull.isNull(o.getAesKey())) {
//                        //随机生成
//                        String aes = Base64.encode(KeyGenerator.getInstance("AES").generateKey().getEncoded());
//                        o.setAesKey(aes);
//                    }
//                } else {
//                    if (ObjectNull.isNull(o.getAesKey())) {
//                        throw new BusinessException("解密不能生成AES密串");
//                    }
//                }
//                break;
//            case MD5:
//            case BASE64:
//                break;
//            default:
//                throw new BusinessException("解密不匹配类型");
//        }
//    }
//
//    @Override
//    public Object execute(EncryptionDto dto, Map<String, Object> params) {
//        if (flag) {
//            return getSecret(dto.getMode());
//        }
//        //加密
//        if (dto.isType()) {
//            return EncryptionUtils.encrypt(dto);
//        } else {
//            return EncryptionUtils.decode(dto);
//        }
//    }
//
//    private Object getSecret(EncryptionEnum mode) {
//        Map map = new HashMap(1);
//        switch (mode) {
//            case SM2:
//                SM2 sm2 = SmUtil.sm2();
//                map.put("publicKey", sm2.getPublicKeyBase64());
//                map.put("privateKey", sm2.getPrivateKeyBase64());
//                return map;
//            case AES:
//                try {
//                    return Base64.encode(KeyGenerator.getInstance("AES").generateKey().getEncoded());
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//            default:
//                return "只支持SM2、AES密钥生成";
//        }
//    }
//}
