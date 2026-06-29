package cn.bctools.message.push.handler.wechatwork.robot;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigEnterriseWeChat;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.enums.MessagePushStatusEnum;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.wechatwork.robot.ImageMessageDTO;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.handler.BaseMessageHandler;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 企业微信-图片消息
 *
 * @author wl*/
@Component
@AllArgsConstructor
public class ImageMessageHandler extends BaseMessageHandler<ImageMessageDTO> {

    private final MessagePushHisService messagePushHisService;
    private final AuthTenantConfigServiceApi configServiceApi;

    @Override
    public void handle(ImageMessageDTO param) {
        if (!param.hasReceiver()) {
            throw new BusinessException("没有检测到接收人配置");
        }
        //获取配置详情
        String body = configServiceApi.key(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION, TenantContextHolder.getTenantId()).getData();
        SysConfigEnterriseWeChat config = configServiceApi.convertKey(ConfigsTypeEnum.ENTERPRISE_WECHAT_APPLICATION_CONFIGURATION, body);

        if (!config.getEnable()) {
            throw new IllegalArgumentException("未找到配置，请核实!");
        }

        //生成批次号
        String batchNumber = UUID.randomUUID().toString().replaceAll("-", "");

        MessagePushHis messagePushHis = new MessagePushHis()
                .setBatchNumber(batchNumber)
                .setMessageContent(JSON.toJSONString(param))
                .setPlatform(PlatformEnum.WECHAT_WORK_ROBOT)
                .setMessageType(MessageTypeEnum.WECHAT_WORK_ROBOT_IMAGE);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("base64", param.getBase64());
            jsonObject.set("md5", param.getMd5());
            List<String> mentionedList = new ArrayList<>();
            List<String> mentionedMobileList = new ArrayList<>();
            param.getDefinedReceivers().forEach(receiverUser -> {
                String receiverConfig = receiverUser.getReceiverConfig();
                receiverConfig = "all".equals(receiverConfig) ? "@all" : receiverConfig; // 补一下at全部人的@符号
                boolean isMobile = ReUtil.isMatch("^[1][3,4,5,6,7,8,9][0-9]{9}$", receiverConfig);
                if (isMobile) {
                    mentionedMobileList.add(receiverConfig);
                } else {
                    mentionedList.add(receiverConfig);
                }
            });

            jsonObject.set("mentioned_list", mentionedList);
            jsonObject.set("mentioned_mobile_list", mentionedMobileList);
            JSONObject messageParam = new JSONObject();
            messageParam.set("msgtype", "image");
            messageParam.set("image", jsonObject);
            String post = HttpUtil.post(config.getWebhook(), messageParam.toString());
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") JSONObject result = new JSONObject(post);
            String errcode = "errcode";
            if (result.getInt(errcode) != 0) {
                throw new WxErrorException(post);
            }
            messagePushHis.setPushStatus(MessagePushStatusEnum.SUCCESS);
        } catch (WxErrorException e) {
            messagePushHis.setPushStatus(MessagePushStatusEnum.FAILED);
            String eMessage = ExceptionUtil.getMessage(e);
            eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
            messagePushHis.setErrorMsg(eMessage);
        }
        messagePushHisService.save(messagePushHis);
    }

    @Override
    public void resend(String pushHisId) throws Exception {
        MessagePushHis his = messagePushHisService.getById(pushHisId);
        ImageMessageDTO dto = JSON.parseObject(his.getMessageContent(), ImageMessageDTO.class);
        handle(dto);
    }
}
