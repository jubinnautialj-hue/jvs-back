package cn.bctools.rule.encryption.desensitization;


/**
 * The enum Desensitization selected option.
 *
 * @author jvs
 */
public enum DesensitizationSelectedOption {
    /**
     * User id desensitization selected option.
     */
    USER_ID("用户id"),
    /**
     * Chinese name desensitization selected option.
     */
    CHINESE_NAME("中文名"),
    /**
     * Id card desensitization selected option.
     */
    ID_CARD("身份证号"),
    /**
     * Fixed phone desensitization selected option.
     */
    FIXED_PHONE("座机号"),
    /**
     * Mobile phone desensitization selected option.
     */
    MOBILE_PHONE("手机号"),
    /**
     * Address desensitization selected option.
     */
    ADDRESS("地址"),
    /**
     * Email desensitization selected option.
     */
    EMAIL("电子邮件"),
    /**
     * Password desensitization selected option.
     */
    PASSWORD("密码"),
    /**
     * Car license desensitization selected option.
     */
    CAR_LICENSE("中国大陆车牌，包含普通车辆、新能源车辆"),
    /**
     * Bank card desensitization selected option.
     */
    BANK_CARD("银行卡"),

    /**
     * Ipv 4 desensitization selected option.
     */
    IPV4("IPv4地址"),

    /**
     * Ipv 6 desensitization selected option.
     */
    IPV6("IPv6地址"),

    /**
     * First mask desensitization selected option.
     */
    FIRST_MASK("定义了一个first_mask的规则，只显示第一个字符");

    /**
     * The Msg.
     */
    public final String msg;

    DesensitizationSelectedOption(String msg) {
        this.msg = msg;
    }
}
