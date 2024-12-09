package cn.bctools.common.enums;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class SysFrameApplyConfig extends SysApplyConfig {

    /**
     * 未设置初始密码，默认123456
     */
    private String defaultPassword = "123456";
    /**
     * true-启用水印，false-不启用水印
     */
    private Boolean watermark;

}
