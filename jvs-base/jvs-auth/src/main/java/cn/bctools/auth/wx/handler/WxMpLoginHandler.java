package cn.bctools.auth.wx.handler;

import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.auth.wx.kefu.AsyncWxKeFuMessage;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigWx;
import cn.bctools.redis.utils.RedisUtils;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author admin
 * [description]：登录
 */
@Slf4j
@Component
public class WxMpLoginHandler implements WxMpMessageHandler {
    private static final String EVENT_KEY_HEAD = "qrscene_";
    @Autowired
    RedisUtils redisUtils;
    @Autowired
    SysConfigsService sysConfigService;
    @Autowired
    AsyncWxKeFuMessage asyncWxKeFuMessage;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
        try {
            SysConfigWx sysWxMpSettings = sysConfigService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
            if (!sysWxMpSettings.getEnable()) {
                log.error("没有配置消息回复");
            }
            WxMpUser wxmpUser = wxMpService.getUserService()
                    .userInfo(wxMessage.getFromUser(), null);
            //判断是否为登录
            String eventKey = wxMessage.getEventKey();
            if (StrUtil.isNotEmpty(eventKey)) {
                //如果是关注 微信会加上qrscene_
                eventKey = eventKey.replaceAll(EVENT_KEY_HEAD, "");
                boolean exists = redisUtils.exists(eventKey);
                if (exists) {
                    redisUtils.setExpire(eventKey, JSONObject.toJSONString(wxmpUser), 1, TimeUnit.HOURS);
                }
            }
            //判断是否为关注 关注才发送消息
            if (StrUtil.isNotEmpty(wxMessage.getEvent()) && wxMessage.getEvent().equals(WxConsts.EventType.SUBSCRIBE)) {
                //异步推送消息
                asyncWxKeFuMessage.asyncSetImageMessage(wxMpService, wxMessage.getFromUser());
                //推送关键字
                asyncWxKeFuMessage.asyncSetTextMessage(wxMpService, wxMessage.getFromUser(), HtmlUtil.cleanHtmlTag(Jsoup.parse(sysWxMpSettings.getKeywordText()).body().toString()));
                return WxMpXmlOutMessage.TEXT()
                        .content(HtmlUtil.cleanHtmlTag(Jsoup.parse(sysWxMpSettings.getWelcomeText()).body().toString()))
                        .fromUser(wxMessage.getToUser())
                        .toUser(wxMessage.getFromUser())
                        .build();
            }
        } catch (Exception e) {
            log.info("微信消息接收，登录判断错误!", e);
        }
        return null;
    }
}
