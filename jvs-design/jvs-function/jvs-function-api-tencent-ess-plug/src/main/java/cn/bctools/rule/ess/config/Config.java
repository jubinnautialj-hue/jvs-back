package cn.bctools.rule.ess.config;

/**
 * @author jvs
 * 基础配置，调用API之前必须填充的参数
 */
public class Config {
    /**
     * API域名，现网使用 ess.tencentcloudapi.com
     */
    public static final String END_POINT = "ess.test.ess.tencent.cn";

    /**
     * 文件服务域名，现网使用 file.ess.tencent.cn
     */
    public static final String FILE_SERVICE_END_POINT = "file.test.ess.tencent.cn";

    /**
     * secretId . 腾讯云ak/sk (secretId/secretKey) 调用API的密钥对，通过腾讯云后台CAM控制台获取
     */
    public static final String SECRET_ID = "****************";
    /**
     * secretKey .腾讯云ak/sk (secretId/secretKey)  调用API的密钥对，通过腾讯云后台CAM控制台获取
     */
    public static final String SECRET_KEY = "****************";

    /**
     * OperatorUserId: 经办人id 即管理员用户id或者员工用户id，电子签控制台获取
     */
    public static final String OPERATOR_USER_ID = "yDSLbUUckpokdjc5UEOgVQFCQadNc2V9";


    /**
     * 以下为业务具体参数，请结合场景具体赋值，本工程仅用于测试
     * 企业方静默签用的印章Id，电子签控制台获取
     */
    public static final String SERVER_SIGN_SEAL_ID = "****************";
    /**
     * 模板Id，电子签控制台获取，仅在通过模板发起时使用
     */
    public static final String TEMPLATE_ID = "yDSLbUUckpokdj6iUEOgVQFSMmJkkIuJ";

}

