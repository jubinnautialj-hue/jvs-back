package cn.bctools.common.enums;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author wl
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PlatformConfig extends SysConfigBase<PlatformConfig> {

    /**
     * 平台名称
     */
    String name;
    /**
     * 平台主域名   bctools.cn
     * 用户多系统相互登陆时需要使用
     */
    String domain;
    /**
     * 平台公告
     */
    String bulletin;
    /**
     * 凭证
     */
    String license = "";

}
