package cn.bctools.message.push.api;

import cn.bctools.common.utils.R;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 发送钉钉消息
 * @author guojing
 */
@FeignClient(value = "message-push-mgr",contextId = "dingtalk-corp")
public interface DingTalkCorpApi {
    String PREFIX = "/ding/talk/corp";

    /**
     * 发送钉钉卡片消息 单个跳转
     * @param dto 消息数据
     * @return 返回状态
     */
    @PostMapping(PREFIX +"/multi")
    R<Boolean> sendActionCardMultiMessage(@RequestBody ActionCardMultiMessageDTO dto);

    /**
     * 重新发送钉钉卡片消息 单个跳转
     * @param pushHisId 消息发送历史id
     * @return 返回状态
     */
    @GetMapping(PREFIX +"/multi/resend")
    R<Boolean> resendActionCardMultiMessage(String pushHisId);

    /**
     * 发送钉钉卡片消息 整体跳转
     * @param dto 消息数据
     * @return 返回状态
     */
    @PostMapping(PREFIX +"/single")
    R<Boolean> sendActionCardSingleMessage(@RequestBody ActionCardSingleMessageDTO dto);

    /**
     * 重新发送钉钉卡片消息 整体跳转
     * @param pushHisId 消息发送历史id
     * @return 返回状态
     */
    @GetMapping(PREFIX +"/single/resend")
    R<Boolean> resendActionCardSingleMessage(String pushHisId);

    /**
     * 发送钉钉链接消息
     * @param dto 消息数据
     * @return 返回状态
     */
    @PostMapping(PREFIX +"/link")
    R<Boolean> sendLinkMessage(@RequestBody LinkMessageDTO dto);

    /**
     * 重新发送钉钉链接消息
     * @param pushHisId 消息发送历史id
     * @return 返回状态
     */
    @GetMapping(PREFIX +"/link/resend")
    R<Boolean> resendLinkMessage(String pushHisId);

    /**
     * 发送钉钉工作通知Markdown
     * @param dto 消息数据
     * @return 返回状态
     */
    @PostMapping(PREFIX +"/markdown")
    R<Boolean> sendMarkdownMessage(@RequestBody MarkdownMessageDTO dto);

    /**
     * 重新发送钉钉工作通知Markdown
     * @param pushHisId 消息发送历史id
     * @return 返回状态
     */
    @GetMapping(PREFIX +"/markdown/resend")
    R<Boolean> resendMarkdownMessage(String pushHisId);

    /**
     * 发送钉钉工作消息-oa
     * @param dto 消息数据
     * @return 返回状态
     */
    @PostMapping(PREFIX +"/oa")
    R<Boolean> sendOaMessage(@RequestBody OaMessageDTO dto);

    /**
     * 重新发送钉钉工作消息-oa
     * @param pushHisId 消息发送历史id
     * @return 返回状态
     */
    @GetMapping(PREFIX +"/oa/resend")
    R<Boolean> resendOaMessage(String pushHisId);

    /**
     * 发送工作通知发送DTO
     * @param dto 消息数据
     * @return 返回状态
     */
    @PostMapping(PREFIX +"/text")
    R<Boolean> sendTextMessage(@RequestBody TextMessageDTO dto);

    /**
     * 发送工作通知发送DTO
     * @param dto 消息数据
     * @return 返回状态
     */
    @PostMapping(PREFIX +"/async/text")
    R<Boolean> sendAsyncTextMessage(@RequestBody TextMessageDTO dto);

    /**
     * 重新发送工作通知发送DTO
     * @param pushHisId 消息发送历史id
     * @return 返回状态
     */
    @GetMapping(PREFIX +"/text/resend")
    R<Boolean> resendTextMessage(String pushHisId);

}
