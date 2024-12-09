package cn.bctools.message.push.service;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import cn.bctools.message.push.dto.vo.WxMpTemplateVo;

import java.util.List;

/**
 * @author xh
 */
public interface WechatOfficialAccountService {

//    boolean sendWebChatMpNewsMessage(NewsMessageDTO messageDto);
//
//    boolean sendWebChatMpTextMessage(TextMessageDTO messageDto);

    /**
     * 公从号消息
     *
     * @param messageDto
     * @param pushUser
     */
    void sendWebChatTemplateMessage(TemplateMessageDTO messageDto);

//    boolean resendWebChatMpNewsMessage(String pushHisId);
//
//    boolean resendWebChatMpTextMessage(String pushHisId);

    /**
     * 发送微信模板消息
     *
     * @param pushHisId
     */
    void resendWebChatTemplateMessage(String pushHisId);

    /**
     * 获取所有模板
     *
     * @return
     */
    List<WxMpTemplateVo> getAllPrivateTemplate();
}
