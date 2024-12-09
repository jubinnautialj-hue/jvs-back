package cn.bctools.common.enums;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xh
 */
@Data
@Accessors(chain = true)
public class SysConfigSms extends SysConfigBase<SysConfigSms> {

    /**
     * 验证吗模版code
     */
    String templateCode;

    String accessKeyId;

    String accessKeySecret;
    /**
     * 签名
     */
    String signature;

}
