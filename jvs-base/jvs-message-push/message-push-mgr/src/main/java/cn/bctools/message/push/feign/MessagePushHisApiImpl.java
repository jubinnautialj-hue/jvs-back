package cn.bctools.message.push.feign;

import cn.bctools.common.utils.BeanCopyUtil;
import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.R;
import cn.bctools.message.push.api.MessagePushHisApi;
import cn.bctools.message.push.dto.enums.MessageTypeEnum;
import cn.bctools.message.push.dto.messagepush.MessagePushHisDto;
import cn.bctools.message.push.entity.MessagePushHis;
import cn.bctools.message.push.service.MessagePushHisService;
import cn.bctools.message.push.service.MessageResendService;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xh
 */
@RequestMapping
@RestController
@AllArgsConstructor
@Api(tags = "[Feign] 消息发送历史")
public class MessagePushHisApiImpl implements MessagePushHisApi {

    private final MessagePushHisService messagePushHisService;
    private final MessageResendService messageResendService;

    @Override
    public R<List<MessagePushHisDto>> getAllPushHis(MessagePushHisDto dto) {
        LambdaQueryWrapper<MessagePushHis> queryWrapper = createQueryWrapper(dto);
        List<MessagePushHis> list = messagePushHisService.list(queryWrapper);
        return R.ok(list.stream().map(this::convert2Dto).collect(Collectors.toList()));
    }

    @Override
    public R<Page<MessagePushHisDto>> pagePushHis(Long current, Long size, MessagePushHisDto dto) {
        LambdaQueryWrapper<MessagePushHis> queryWrapper = createQueryWrapper(dto);
        Page<MessagePushHis> queryPage = new Page<>();
        queryPage.setCurrent(current).setSize(size);
        messagePushHisService.page(queryPage, queryWrapper);

        Page<MessagePushHisDto> page = BeanCopyUtil.copy(queryPage,Page.class);
        List<MessagePushHisDto> records = queryPage.getRecords().stream().map(this::convert2Dto).collect(Collectors.toList());
        page.setRecords(records);
        return R.ok(page);
    }

    @Override
    public R<Boolean> resend(String hisId, String msgType) {
        messageResendService.resendMessage(hisId, MessageTypeEnum.valueOf(msgType));
        return R.ok();
    }

    private LambdaQueryWrapper<MessagePushHis> createQueryWrapper(MessagePushHisDto dto) {
        return new LambdaQueryWrapper<MessagePushHis>()
                .eq(ObjectUtil.isNotNull(dto.getBatchNumber()), MessagePushHis::getBatchNumber, dto.getBatchNumber())
                .eq(ObjectUtil.isNotNull(dto.getPlatform()), MessagePushHis::getPlatform, dto.getPlatform())
                .eq(ObjectUtil.isNotNull(dto.getMessageType()), MessagePushHis::getMessageType, dto.getMessageType())
                .eq(ObjectUtil.isNotNull(dto.getPushStatus()), MessagePushHis::getPushStatus, dto.getPushStatus())
                .eq(ObjectUtil.isNotNull(dto.getClientCode()), MessagePushHis::getClientCode, dto.getClientCode())
                .eq(ObjectUtil.isNotNull(dto.getUserId()), MessagePushHis::getUserId, dto.getUserId())
                .between(ObjectNull.isNotNull(dto.getQueryStartTime()) && ObjectNull.isNotNull(dto.getQueryEndTime()), MessagePushHis::getCreateTime, dto.getQueryStartTime(), dto.getQueryEndTime())
                .orderByDesc(MessagePushHis::getCreateTime);
    }

    private MessagePushHis convert2Entity(MessagePushHisDto source) {
        if (source == null) {
            return new MessagePushHis();
        }
        MessagePushHis messagePushHis = BeanCopyUtil.copy(source,MessagePushHis.class);
        if (source.getPlatform() != null) {
            messagePushHis.setPlatform(source.getPlatform());
        }
        if (source.getMessageType() != null) {
            messagePushHis.setMessageType(source.getMessageType());
        }
        if (source.getPushStatus() != null) {
            messagePushHis.setPushStatus(source.getPushStatus());
        }
        return messagePushHis;
    }

    private MessagePushHisDto convert2Dto(MessagePushHis source) {
        if (source == null) {
            return new MessagePushHisDto();
        }
        MessagePushHisDto messagePushHisDto = BeanCopyUtil.copy(source,MessagePushHisDto.class);

        if (source.getPlatform() != null) {
            messagePushHisDto.setPlatform(source.getPlatform()).setPlatformName(source.getPlatform().getDesc());
        }
        if (source.getMessageType() != null) {
            messagePushHisDto.setMessageType(source.getMessageType());
        }
        if (source.getPushStatus() != null) {
            messagePushHisDto.setPushStatus(source.getPushStatus());
            messagePushHisDto.setPushStatusName(source.getPushStatus().getDesc());
        }

        return messagePushHisDto;
    }
}
