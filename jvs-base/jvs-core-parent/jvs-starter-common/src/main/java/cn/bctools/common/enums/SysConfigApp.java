package cn.bctools.common.enums;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 移动端
 */
@Data
@Accessors(chain = true)
public class SysConfigApp extends SysConfigBase<SysConfigApp> {
    /**
     * 应用中心显示的图片
     */
    private String appBanner;

    /**
     * 版本号
     */
    private String version;

    /**
     * 官方微信
     */
    private String wx;

    /**
     * 客服电话
     */
    private String phone;
}
