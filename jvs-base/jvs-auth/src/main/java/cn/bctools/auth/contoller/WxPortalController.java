package cn.bctools.auth.contoller;

import cn.bctools.auth.service.SysConfigsService;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigWx;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.util.WxMpConfigStorageHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author Binary Wang(https://github.com/binarywang)
 * 微信消息接收处理
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/wx/portal/{appId}")
public class WxPortalController {

    /**
     * 默认加密方式
     */
    private static final String DEFAULT_ENCODE_TYPE = "aes";

    private final WxMpService wxService;
    private final WxMpMessageRouter messageRouter;
    private final SysConfigsService sysConfigService;

    @GetMapping
    public String authGet(@PathVariable("appId") String appId,
                          @RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature,
                timestamp, nonce, echostr);
        String tenantId = TenantContextHolder.getTenantId();
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }

        if (!this.wxService.switchover(appId)) {
            SysConfigWx config = sysConfigService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
            if (!config.getEnable()) {
                throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
            }
            WxMpDefaultConfigImpl obj = BeanCopyUtil.copy(config, WxMpDefaultConfigImpl.class);
            obj.setSecret(config.getAppSecret());
            //添加一个新的配置信息
            wxService.addConfigStorage(appId, obj);
            WxMpConfigStorageHolder.set(appId);
        }

        if (wxService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }

        //如果不对，删除数据
        wxService.removeConfigStorage(appId);

        return "非法请求";
    }

    @PostMapping
    @SneakyThrows
    public String post(@PathVariable("appId") String appId,
                       @RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        log.info("\n接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

        String tenantId = TenantContextHolder.getTenantId();
        boolean switchover = this.wxService.switchover(appId);
        if (!switchover) {
            SysConfigWx config = sysConfigService.getConfig(ConfigsTypeEnum.WECHAT_OFFICIAL_ACCOUNT_CONFIGURATION);
            if (!config.getEnable()) {
                throw new IllegalArgumentException(String.format("未找到对应appid=[%s]的配置，请核实！", appId));
            }
            WxMpDefaultConfigImpl obj = BeanCopyUtil.copy(config, WxMpDefaultConfigImpl.class);

            obj.setSecret(config.getAppSecret());
            //添加一个新的配置信息
            wxService.addConfigStorage(appId, obj);
        }
        WxMpConfigStorageHolder.set(appId);
        if (!wxService.checkSignature(timestamp, nonce, signature)) {
            //如果不对，删除数据
            wxService.removeConfigStorage(appId);
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }
        String out = null;
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            log.info("消息解密后内容为：{} ", inMessage.toString());
            WxMpUser userWxInfo = wxService.getUserService()
                    .userInfo(inMessage.getFromUser(), null);
            log.info("获取的用户信息为：{}", JSONUtil.toJsonStr(userWxInfo));
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toXml();
        } else if (DEFAULT_ENCODE_TYPE.equalsIgnoreCase(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxService.getWxMpConfigStorage(),
                    timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toEncryptedXml(wxService.getWxMpConfigStorage());
        }

        log.debug("\n组装回复信息：{}", out);
        return out;
    }

    private WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return this.messageRouter.route(message);
        } catch (Exception e) {
            log.error("路由消息时出现异常！", e);
        }

        return null;
    }

}
