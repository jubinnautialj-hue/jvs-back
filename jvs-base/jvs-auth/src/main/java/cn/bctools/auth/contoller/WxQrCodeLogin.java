package cn.bctools.auth.contoller;

import cn.bctools.auth.login.dto.WxQrCodeCheckDto;
import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.auth.wx.WxMpConfig;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigWx;
import cn.bctools.common.utils.R;
import cn.bctools.log.annotation.Log;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.json.JSONUtil;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpQrcodeService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author ：admin
 * 获取微信登录二维码
 * 和校验微信二维码,如果校验通过后.自动完成登陆
 */
@RestController
@AllArgsConstructor
@RequestMapping("/wx/qr/code")
@Slf4j
public class WxQrCodeLogin {
    private final RedisUtils redisUtils;
    private final WxMpConfig wxMpConfig;
    private final WxMpService wxMpService;
    private final SysConfigsService configsService;

    @GetMapping("/login/{loginId}")
    @SneakyThrows
    public R<String> loginQr(@ApiParam(value = "前端唯一id用于确认登录账号") @PathVariable("loginId") String loginId) {
        //通过租户获取数据
        SysConfigWx config = configsService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
        WxMpQrcodeService qrcodeService = null;
        if (config.getEnable()) {
            wxMpConfig.buildWxMpConfigStorage(config);
            //设置上上文为这个公众号
            WxMpConfigStorageHolder.set(config.getAppKey());
            qrcodeService = wxMpService.getQrcodeService();
        }
        WxMpQrCodeTicket wxMpQrCodeTicket = null;
        try {
            wxMpQrCodeTicket = qrcodeService.qrCodeCreateTmpTicket(loginId, 5 * 60);
        } catch (WxErrorException e) {
            log.error("微信获取二维码错误");
            String message = e.getMessage();
            if (message.contains("错误代码：")) {
                return R.failed(e.getMessage().split(",")[0]);
            }
            return R.failed(e.getMessage());
        }
        log.info("获取的ticket数据为:{}", JSONUtil.toJsonStr(wxMpQrCodeTicket));
        String s = qrcodeService.qrCodePictureUrl(wxMpQrCodeTicket.getTicket());
        redisUtils.setExpire(loginId, "", 5L, TimeUnit.MINUTES);
        return R.ok(s);
    }

    /**
     * 用于前端判断二维码 是否扫码成功 并进入微信公众号
     */
    @SneakyThrows
    @GetMapping("/check/{loginId}")
    public R<WxQrCodeCheckDto> getQrCode(@ApiParam(value = "前端生成的唯一id", required = true) @PathVariable String loginId) {
        WxQrCodeCheckDto wxQrCodeCheckDto = new WxQrCodeCheckDto();
        boolean exists = redisUtils.exists(loginId);
        if (exists) {
            Object o = redisUtils.get(loginId);
            if (ObjectUtils.isNotEmpty(o)) {
                wxQrCodeCheckDto.setCheckStatus(Boolean.TRUE);
            }
            return R.ok(wxQrCodeCheckDto);
        }
        wxQrCodeCheckDto.setIsPastDue(Boolean.TRUE);
        return R.ok(wxQrCodeCheckDto);
    }
}
