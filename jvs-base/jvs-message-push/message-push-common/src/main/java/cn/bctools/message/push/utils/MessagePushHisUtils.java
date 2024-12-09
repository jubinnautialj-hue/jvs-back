package cn.bctools.message.push.utils;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.TenantContextHolder;
import cn.bctools.database.util.IdGenerator;
import cn.bctools.message.push.dto.enums.MessagePushStatusEnum;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.enums.PlatformEnum;
import cn.bctools.message.push.dto.messagepush.BaseMessage;
import cn.bctools.message.push.dto.messagepush.InsideNotificationDto;
import cn.bctools.message.push.dto.messagepush.ReceiversDto;
import cn.bctools.message.push.entity.InsideNotice;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.service.InsideNoticeService;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xh
 */
@Slf4j
@Component
@AllArgsConstructor
public class MessagePushHisUtils {
    private final MessagePushHisService messagePushHisService;
    private final JvsUserComponent jvsUserComponent;
    private final InsideNoticeService insideNoticeService;

    /**
     * 储存消息发送日志
     *
     * @param params          消息数据
     * @param platformEnum    平台类型
     * @param messageTypeEnum 消息类型
     * @return 消息批次号
     */
    public String saveHis(BaseMessage params, PlatformEnum platformEnum, MessageTypeEnum messageTypeEnum) {
        String tenantId = params.getTenantId();
        log.info("==========租户id: {}=============",tenantId);
        TenantContextHolder.setTenantId(tenantId);
        String batchNumber = IdGenerator.getIdStr();
        List<ReceiversDto> receiversDtos = params.getDefinedReceivers();
        if (ObjectNull.isNull(receiversDtos)) {
            throw new BusinessException("没有检测到接收人配置");
        }
        if (receiversDtos.isEmpty()) {
            throw new BusinessException("没有检测到接收人配置");
        }
        switch (platformEnum){
            case EMAIL:
                jvsUserComponent.setEmailConfig(receiversDtos);
                break;
            case ALI_SMS:
                jvsUserComponent.setAliSmsConfig(receiversDtos);
                break;
            case WECHAT_OFFICIAL_ACCOUNT:
                jvsUserComponent.setWxMpConfig(receiversDtos);
                break;
            case INSIDE_NOTIFICATION:
                jvsUserComponent.setInsideNoticeConfig(receiversDtos,params.getTenantId());
                this.saveNoticeMessage(receiversDtos, params, batchNumber);
                break;
            case DING_TALK_CORP:
                jvsUserComponent.setDingCropConfig(receiversDtos);
                break;
            default:
        }

        params.setDefinedReceivers(receiversDtos);
        List<MessagePushHis> pushHisList = new ArrayList<>();
        receiversDtos.forEach(receiver -> {
            MessagePushHis messagePushHis = new MessagePushHis()
                    .setBatchNumber(batchNumber)
                    .setPlatform(platformEnum)
                    .setMessageType(messageTypeEnum)
                    .setMessageContent(JSON.toJSONString(params))
                    .setClientCode(params.getClientCode())
                    .setUserId(receiver.getUserId())
                    .setUserName(receiver.getUserName())
                    .setTenantId(params.getTenantId());
            messagePushHis.setCreateById(params.getCreateById());
            messagePushHis.setCreateBy(params.getCreateBy());
            messagePushHis.setUpdateBy(params.getUpdateBy());
            if (PlatformEnum.INSIDE_NOTIFICATION.equals(platformEnum)) {
                messagePushHis.setPushStatus(MessagePushStatusEnum.SUCCESS);
            }
            log.info("微信公众号用户属性：用户：{}，openId：{}，全属性：{}", receiver.getUserName(), receiver.getReceiverConfig(), receiver.toString());
            pushHisList.add(messagePushHis);
        });
        messagePushHisService.saveBatch(pushHisList);
        return batchNumber;
    }

    /**
     * 保存站内信记录
     *
     * @param receiversDtos 接收人
     * @param dto           消息内容
     * @param batchNumber   消息发送批次号
     */
    public void saveNoticeMessage(List<ReceiversDto> receiversDtos, BaseMessage dto, String batchNumber) {
        List<InsideNotice> noticeList = new ArrayList<>();
        InsideNotificationDto notificationDto = (InsideNotificationDto) dto;
        String noticeAvatar = notificationDto.getLogo();
        receiversDtos.forEach(receiversDto -> {
            String tenantId = this.getTenantId(receiversDto);
            InsideNotice insideNotice = new InsideNotice()
                    .setReadIs(Boolean.FALSE)
                    .setUserId(receiversDto.getUserId())
                    .setUserName(receiversDto.getUserName())
                    .setMsgContent(notificationDto.getContent())
                    //消息批次号
                    .setBatchNumber(batchNumber)
                    //终端key
                    .setClientCode(notificationDto.getClientCode())
                    //大类
                    .setLargeCategories(notificationDto.getLargeCategories())
                    //小类
                    .setSubClass(notificationDto.getSubClass())
                    //回调地址
                    .setCallBackUrl(notificationDto.getCallBackUrl())
                    //消息头像
                    .setNoticeAvatar(noticeAvatar)
                    //设置租户id
                    .setTenantId(tenantId);
            insideNotice.setCreateById(notificationDto.getCreateById());
            insideNotice.setCreateBy(notificationDto.getCreateBy());
            insideNotice.setUpdateBy(notificationDto.getUpdateBy());
            noticeList.add(insideNotice);
        });
        insideNoticeService.saveBatch(noticeList);
    }

    public String getTenantId(ReceiversDto receiversDto) {
        return StrUtil.isBlank(TenantContextHolder.getTenantId()) ||
                ObjectUtil.equals(TenantContextHolder.getTenantId(), JvsUserComponent.DEFAULT_TENANT_ID)
                ? receiversDto.getTenantId() : TenantContextHolder.getTenantId();
    }
}
