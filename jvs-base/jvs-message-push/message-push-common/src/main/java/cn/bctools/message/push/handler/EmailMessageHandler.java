package cn.bctools.message.push.handler;

import cn.bctools.auth.api.api.AuthTenantConfigServiceApi;
import cn.bctools.common.enums.ConfigsTypeEnum;
import cn.bctools.common.enums.SysConfigMail;
import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.SpringContextUtil;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.message.push.dto.config.EmailConfig;
import cn.bctools.message.push.dto.enums.MessagePushStatusEnum;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.EmailMessageDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.bctools.message.push.utils.JvsUserComponent;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 邮件消息处理器
 *
 * @author xh
 */
@Slf4j
@Component
@AllArgsConstructor
public class EmailMessageHandler extends BaseMessageHandler<EmailMessageDto> {

    private final MessagePushHisService messagePushHisService;
    private final EmailConfig emailConfig;
    private final AuthTenantConfigServiceApi configServiceApi;
    private final JvsUserComponent jvsUserComponent;

    @Override
    public void handle(EmailMessageDto messageDto) {
        //生成批次号
        String batchNumber = IdWorker.get32UUID();
        if (!messageDto.hasReceiver()) {
            MessagePushHis messagePushHis = new MessagePushHis()
                    .setBatchNumber(batchNumber)
                    .setPlatform(PlatformEnum.EMAIL)
                    .setMessageType(MessageTypeEnum.EMAIL)
                    .setMessageContent(JSON.toJSONString(messageDto))
                    .setClientCode(messageDto.getClientCode());
            messagePushHisService.save(messagePushHis);
            throw new BusinessException("没有检测到接收人配置");
        }
        jvsUserComponent.setEmailConfig(messageDto.getDefinedReceivers());

        //获取配置详情
        String body = configServiceApi.key(ConfigsTypeEnum.MAIL_CONFIGURATION, TenantContextHolder.getTenantId()).getData();
        SysConfigMail config = configServiceApi.convertKey(ConfigsTypeEnum.MAIL_CONFIGURATION, body);
        if (!config.getEnable()) {
            throw new IllegalArgumentException("未找到邮件配置，请核实!");
        }

        MailAccount account = BeanCopyUtil.copy(config, MailAccount.class);

        List<MessagePushHis> hisList = new ArrayList<>();

        messageDto.getDefinedReceivers().forEach(e -> {
            MessagePushHis messagePushHis = new MessagePushHis();
            try {
                messagePushHis.setMessageContent(JSON.toJSONString(messageDto))
                        .setBatchNumber(batchNumber)
                        .setUserId(e.getUserId())
                        .setUserName(e.getUserName())
                        .setPlatform(PlatformEnum.EMAIL)
                        .setMessageType(MessageTypeEnum.EMAIL);
                account.setSslEnable(true);
                MailUtil.send(account, e.getReceiverConfig(), messageDto.getTitle(), messageDto.getContent(), false);
                messagePushHis.setPushStatus(MessagePushStatusEnum.SUCCESS);
            } catch (Exception exception) {
                messagePushHis.setPushStatus(MessagePushStatusEnum.FAILED);
                String eMessage = ExceptionUtil.getMessage(exception);
                eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
                messagePushHis.setErrorMsg(eMessage);
            }
            hisList.add(messagePushHis);
        });

        messagePushHisService.saveBatch(hisList);
    }

    public void handle(String batchNumber) {
        List<MessagePushHis> messagePushHisList = messagePushHisService.getNoSuccessHisList(batchNumber);
        this.send(messagePushHisList, batchNumber);
        this.verifyAgain(messagePushHisList, batchNumber, 2);
    }

    private void send(List<MessagePushHis> messagePushHisList, String batchNumber) {
        if (messagePushHisList.isEmpty()) {
            log.info("当前批次号{}未找到消息记录", batchNumber);
            return;
        }

        String body = configServiceApi.key(ConfigsTypeEnum.MAIL_CONFIGURATION, TenantContextHolder.getTenantId()).getData();
        SysConfigMail config = configServiceApi.convertKey(ConfigsTypeEnum.MAIL_CONFIGURATION, body);
        if (!config.getEnable()) {
            throw new IllegalArgumentException("未找到邮件配置，请核实!");
        }
        MailAccount account = BeanCopyUtil.copy(config, MailAccount.class);

        messagePushHisList.forEach(his -> {
            EmailMessageDto hisDto = JSONUtil.toBean(his.getMessageContent(), EmailMessageDto.class);
            hisDto.getDefinedReceivers().removeIf(e -> e.getUserId() == null || !e.getUserId().equals(his.getUserId()));
            if (!hisDto.getDefinedReceivers().isEmpty()) {
                ReceiversDto receiversDto = hisDto.getDefinedReceivers().stream().findFirst().get();
                try {
                    account.setSslEnable(true);
                    MailUtil.send(account, receiversDto.getReceiverConfig(), hisDto.getTitle(), hisDto.getContent(), true);
                    log.info("批次号为{}接收人为{}的邮件发送成功", batchNumber, his.getUserName());
                    his.setPushStatus(MessagePushStatusEnum.SUCCESS);
                } catch (Exception exception) {
                    log.error("email send error", exception);
                    his.setPushStatus(MessagePushStatusEnum.FAILED);
                    String eMessage = ExceptionUtil.getMessage(exception);
                    eMessage = StringUtils.isBlank(eMessage) ? "未知错误" : eMessage;
                    his.setErrorMsg(eMessage);
                }
            } else {
                his.setErrorMsg(SpringContextUtil.msg("重发失败"));
                his.setPushStatus(MessagePushStatusEnum.FAILED);
            }
        });
        messagePushHisService.updateBatchById(messagePushHisList);
    }

    /**
     * 再次发送检查
     *
     * @param pushHisList 发送历史
     * @param batchNumber 消息批次号
     */
    private void verifyAgain(List<MessagePushHis> pushHisList, String batchNumber, int cycleNum) {
        if (cycleNum <= 0) {
            return;
        }
        List<MessagePushHis> messagePushHisList = pushHisList.stream().filter(e -> MessagePushStatusEnum.FAILED.equals(e.getPushStatus())).collect(Collectors.toList());
        if (messagePushHisList.isEmpty()) {
            return;
        }
        this.send(messagePushHisList, batchNumber);

        cycleNum--;
        this.verifyAgain(messagePushHisList, batchNumber, cycleNum);
    }

    @Override
    public void resend(String pushHisId) throws Exception {
        MessagePushHis his = messagePushHisService.getById(pushHisId);
        EmailMessageDto dto = JSON.parseObject(his.getMessageContent(), EmailMessageDto.class);
        dto.getDefinedReceivers().removeIf(e -> e.getUserId() == null || !e.getUserId().equals(his.getUserId()));
        handle(dto);
    }

    private MailAccount createAccount() {
        MailAccount account = new MailAccount();
        account.setHost(emailConfig.getHost());
        account.setPort(emailConfig.getPort());
        account.setAuth(true);
        account.setFrom(emailConfig.getFrom());
        account.setUser(emailConfig.getFrom());
        account.setPass(emailConfig.getPass());
        return account;
    }
}
