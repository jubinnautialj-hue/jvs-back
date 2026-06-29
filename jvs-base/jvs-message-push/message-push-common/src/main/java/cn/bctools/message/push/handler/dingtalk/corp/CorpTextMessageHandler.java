package cn.bctools.message.push.handler.dingtalk.corp;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigDing;
import cn.bctools.common.enums.SysConfigSms;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.config.AliSmsConfig;
import cn.bctools.message.push.dto.enums.MessagePushStatusEnum;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.AliSmsDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.dto.messagepush.dingtalk.corp.TextMessageDTO;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.handler.BaseMessageHandler;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.bctools.message.push.utils.SingletonUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.taobao.api.ApiException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 钉钉工作通知文本类型消息处理器
 *
 * @author xh
 */
@Slf4j
@Component
@AllArgsConstructor
public class CorpTextMessageHandler extends BaseMessageHandler<TextMessageDTO> {

    private final MessagePushHisService messagePushHisService;
    private AuthTenantConfigServiceApi configServiceApi;

    @Override
    public void handle(TextMessageDTO param) {
        if (!param.hasReceiver()) {
            throw new BusinessException("没有检测到接收人配置");
        }
        String body = configServiceApi.key(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION, TenantContextHolder.getTenantId()).getData();
        SysConfigDing config = configServiceApi.convertKey(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION, body);
        if (!config.getEnable()) {
            throw new IllegalArgumentException("未找到钉钉配置，请核实!");
        }
        config.setAppKey(config.getAppId());
        //获取接收人
        List<String> receiverUsers = param.getDefinedReceivers().stream().map(ReceiversDto::getReceiverConfig).collect(Collectors.toList());

        DingTalkClient dingTalkClient = SingletonUtil.get("dinging-" + config.getAppKey() + config.getAppSecret(),
                (SingletonUtil.Factory<DingTalkClient>) () -> new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2"));
        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(Long.valueOf(config.getAgentId()));
        request.setUseridList(String.join(",", receiverUsers));
        request.setDeptIdList(param.getDeptIdList());
        request.setToAllUser(param.getToAllUser());

        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setMsgtype("text");
        msg.setText(new OapiMessageCorpconversationAsyncsendV2Request.Text());
        msg.getText().setContent(param.getContent());
        request.setMsg(msg);

        //生成批次号
        String batchNumber = UUID.randomUUID().toString().replaceAll("-", "");

        MessagePushHis messagePushHis = new MessagePushHis()
                .setBatchNumber(batchNumber)
                .setMessageContent(JSON.toJSONString(param))
                .setPlatform(PlatformEnum.DING_TALK_CORP)
                .setMessageType(MessageTypeEnum.DING_TALK_COPR_TEXT);

        try {
            OapiMessageCorpconversationAsyncsendV2Response rsp = dingTalkClient.execute(request, AccessTokenUtils.getAccessToken(config.getAppKey(), config.getAppSecret()));
            if (!rsp.isSuccess()) {
                throw new IllegalStateException(rsp.getBody());
            }
            messagePushHis.setPushStatus(MessagePushStatusEnum.SUCCESS);
        } catch (ApiException e) {
            messagePushHis.setPushStatus(MessagePushStatusEnum.FAILED);
            String eMessage = ExceptionUtil.getMessage(e);
            eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
            messagePushHis.setErrorMsg(eMessage);
        }
        messagePushHisService.save(messagePushHis);
    }

    /**
     * 根据批次号 发送钉钉消息
     * @param batchNumber 发送批次号
     */
    public void handle(String batchNumber) {
        List<MessagePushHis> messagePushHisList = messagePushHisService.getNoSuccessHisList(batchNumber);
        this.send(messagePushHisList, batchNumber);
    }

    private void send(List<MessagePushHis> messagePushHisList, String batchNumber) {
        if (messagePushHisList.isEmpty()) {
            log.info("当前批次号{}未找到消息记录", batchNumber);
            return;
        }
        //查找钉钉配置
        String body = configServiceApi.key(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION, TenantContextHolder.getTenantId()).getData();
        SysConfigDing config = configServiceApi.convertKey(ConfigsTypeEnum.NAIL_APPLICATION_CONFIGURATION, body);
        if (!config.getEnable()) {
            throw new IllegalArgumentException("未找到钉钉配置，请核实!");
        }
        config.setAppKey(config.getAppId());
        DingTalkClient dingTalkClient = SingletonUtil.get("dinging-" + config.getAppKey() + config.getAppSecret(),
                (SingletonUtil.Factory<DingTalkClient>) () -> new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2"));

        messagePushHisList.forEach(his -> {
            try {
                TextMessageDTO hisDto = JSONUtil.toBean(his.getMessageContent(), TextMessageDTO.class);
                List<String> dingConfigs = hisDto.getDefinedReceivers().stream().map(ReceiversDto::getReceiverConfig).filter(StrUtil::isNotBlank).collect(Collectors.toList());
                if(CollectionUtil.isEmpty(dingConfigs)){
                    throw new BusinessException("未配置dingding接收人数据");
                }
                OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
                request.setAgentId(Long.valueOf(config.getAgentId()));
                request.setUseridList(String.join(",",dingConfigs));
                request.setDeptIdList(hisDto.getDeptIdList());
                request.setToAllUser(hisDto.getToAllUser());
                OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
                msg.setMsgtype("text");
                msg.setText(new OapiMessageCorpconversationAsyncsendV2Request.Text());
                msg.getText().setContent(hisDto.getContent());
                request.setMsg(msg);

                OapiMessageCorpconversationAsyncsendV2Response rsp = dingTalkClient.execute(request, AccessTokenUtils.getAccessToken(config.getAppKey(), config.getAppSecret()));
                if (!rsp.isSuccess()) {
                    throw new IllegalStateException(rsp.getBody());
                }
                his.setPushStatus(MessagePushStatusEnum.SUCCESS);
            } catch (ApiException apiException) {
                his.setPushStatus(MessagePushStatusEnum.FAILED);
                String eMessage = ExceptionUtil.getMessage(apiException);
                eMessage = StringUtils.isBlank(eMessage) ? "api调用异常" : eMessage;
                his.setErrorMsg(eMessage);
            } catch (Exception e){
                his.setPushStatus(MessagePushStatusEnum.FAILED);
                String eMessage = ExceptionUtil.getMessage(e);
                eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
                his.setErrorMsg(eMessage);
            }
        });
        messagePushHisService.updateBatchById(messagePushHisList);
    }

    @Override
    public void resend(String pushHisId) throws Exception {
        MessagePushHis his = messagePushHisService.getById(pushHisId);
        TextMessageDTO dto = JSON.parseObject(his.getMessageContent(), TextMessageDTO.class);
        dto.getDefinedReceivers().removeIf(e -> e.getUserId() == null || !e.getUserId().equals(his.getUserId()));
        handle(dto);
    }
}
