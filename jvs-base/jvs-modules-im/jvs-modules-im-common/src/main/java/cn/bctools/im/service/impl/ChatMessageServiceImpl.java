package cn.bctools.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bctools.im.dto.history.HistoryMessageReqDto;
import cn.bctools.im.entity.ChatMessage;
import cn.bctools.im.entity.enums.MessageTypeEnum;
import cn.bctools.im.mapper.ChatMessageMapper;
import cn.bctools.im.message.dto.BaseSyncMessageDto;
import cn.bctools.im.service.ImMessageService;
import cn.bctools.im.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * @author ZhuXiaoKang
 * @Description 私聊全量消息服务实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ImMessageService {

    @Override
    public MessageTypeEnum getType() {
        return MessageTypeEnum.CHAT_PRIVATE;
    }

    @Override
    public void saveMessage(BaseSyncMessageDto syncMessageDto) {
        ChatMessage chatMessage = new ChatMessage();
        BeanUtils.copyProperties(syncMessageDto, chatMessage);
        chatMessage.setCreateTime(DateUtil.millisecondToLocalDateTime(syncMessageDto.getCreateTime()));
        save(chatMessage);
    }

    /**
     * 查询用户指定好友聊天历史记录, 按时间顺序排序
     *
     * @param reqDto
     * @return
     */
    @Override
    public Page<String> pageMessageHistory(HistoryMessageReqDto reqDto) {
        if (reqDto.getFromUserId() == null) {
            return null;
        }

        // 查询私聊历史消息
        LambdaQueryWrapper<ChatMessage> queryWrapper = Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getTenantId, reqDto.getTenantId())
                .eq(ChatMessage::getToUserId, reqDto.getUserId())
                .eq(ChatMessage::getFromUserId, reqDto.getFromUserId())
                .ge(ChatMessage::getCreateTime, DateUtil.millisecondToLocalDateTime(reqDto.getBeginTime()));
        if (reqDto.getEndTime() != null) {
            queryWrapper.le(ChatMessage::getCreateTime, DateUtil.millisecondToLocalDateTime(reqDto.getEndTime()));
        }
        queryWrapper
                .select(ChatMessage::getMessage)
                .orderByAsc(ChatMessage::getCreateTime);

        Page<ChatMessage> page = new Page<>(reqDto.getCurrent(), reqDto.getSize());
        Page<ChatMessage> resultMsg = page(page, queryWrapper);

        // 封装返回值
        Page<String> resDto = new Page<>();
        BeanUtils.copyProperties(resultMsg, resDto);
        resDto.setRecords(resultMsg.getRecords().stream().map(ChatMessage::getMessage).collect(Collectors.toList()));
        return resDto;
    }
}
