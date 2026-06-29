package cn.bctools.auth.wx;

import cn.bctools.auth.entity.enums.SysConfigTypeEnum;
import cn.bctools.common.enums.SysConfigWx;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.stereotype.Component;

/**
 * @author zhuxiaokang
 * 微信公众号配置
 */

@Slf4j
@Component
@AllArgsConstructor
public class WxMpConfig {
    private final WxMpService wxMpService;

    public WxMpDefaultConfigImpl buildWxMpConfigStorage(SysConfigWx config) {
        WxMpDefaultConfigImpl wxMpConfigStorage = new WxMpDefaultConfigImpl();
        wxMpConfigStorage.setAppId(config.getAppKey());
        wxMpConfigStorage.setAesKey(config.getAesKey());
        wxMpConfigStorage.setSecret(config.getAppSecret());
        wxMpConfigStorage.setToken(config.getToken());
        wxMpConfigStorage.setExpiresTime(System.currentTimeMillis() + 7200);
        wxMpService.addConfigStorage(config.getAppKey(), wxMpConfigStorage);
        return wxMpConfigStorage;
    }
}
