package cn.bctools.rule.md5;

import cn.bctools.rule.annotations.Rule;
import cn.bctools.rule.entity.enums.ClassType;
import cn.bctools.rule.entity.enums.RuleGroup;
import cn.bctools.rule.function.BaseCustomFunctionInterface;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author guojing
 */
@Slf4j
@Rule(value = "MD5",
        group = RuleGroup.加密插件,
        test = true,
        returnType = ClassType.文本,
        order = 10,
//        iconUrl = "rule-hsmjiamifuwu",
        explain = "一种被广泛使用的密码散列函数"
)
public class Md5ServiceImpl implements BaseCustomFunctionInterface<Md5Dto> {

    @Override
    public Object execute(Md5Dto dto, Map<String, Object> params) {
        return (getMD5Hash(dto.getText()));
    }

    public static String getMD5Hash(String input) {
        try {
            // 获取一个 MessageDigest 实例，指定为 MD5 算法
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 计算输入字符串的字节数组的 MD5 哈希值
            byte[] messageDigest = md.digest(input.getBytes());

            // 将字节数组转换为十六进制的字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
