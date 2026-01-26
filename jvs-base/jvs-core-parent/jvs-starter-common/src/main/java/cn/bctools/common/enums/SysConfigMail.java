package cn.bctools.common.enums;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author xh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysConfigMail extends SysConfigBase<SysConfigMail> {

    /**
     * 邮件地址
     */
    private String host;
    /**
     * 密码
     */
    private String pass;
    /**
     * 发送人
     */
    private String from;

}
