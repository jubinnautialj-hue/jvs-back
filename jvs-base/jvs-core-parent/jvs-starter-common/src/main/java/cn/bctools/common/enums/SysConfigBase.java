package cn.bctools.common.enums;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @param <T>
 * @author xh
 */
@Data
@Accessors(chain = true)
public class SysConfigBase<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 是否可用
     */
    Boolean enable;
    /**
     * 租户id
     */
    String tenantId;
    /**
     * 是否启用扫码
     */
    Boolean enableScan = false;
    /**
     * 登陆后回调信息
     */
    String redirectUri;
}
