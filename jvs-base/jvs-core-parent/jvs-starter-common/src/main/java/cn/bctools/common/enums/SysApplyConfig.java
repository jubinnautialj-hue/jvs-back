package cn.bctools.common.enums;

import cn.bctools.common.utils.ObjectNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class SysApplyConfig extends SysConfigBase<SysApplyConfig> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ICON
     */
    private String icon = "/jvs-ui-public/img/icon.png";
    /**
     * logo
     */
    private String logo = "/jvs-ui-public/img/logo.jpg";
    /**
     * 背景,租户名称
     */
    private String bgImg = "/jvs-ui-public/img/bgImg.png";
    /**
     * 自定义域名或Ip端口不包含http
     */
    private String domainName;
    /**
     * 系统名称,这个租户,这个应用的系统名称
     */
    private String systemName = "Jvs 一站式技术供应商 [和JVS一起IT构建，私有化、低风险、高性价、渐进式的数字化转型]";
    /**
     * 是否关闭登录页，需要在配置了三方授权时才判断此逻辑,true 为关闭 false 为打开,默认为 关闭
     */
    private Boolean skipLogin = true;

    public String getIcon() {
        return ObjectNull.isNull(icon) ? "/jvs-ui-public/img/icon.png" : icon;
    }

    public String getLogo() {
        return ObjectNull.isNull(logo) ? "/jvs-ui-public/img/logo.png" : logo;
    }

    public String getBgImg() {
        return ObjectNull.isNull(bgImg) ? "/jvs-ui-public/img/bgImg.png" : bgImg;
    }
}
