package cn.bctools.im.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bctools.im.dto.history.HistoryMessageReqDto;
import cn.bctools.im.entity.ChatGroupMessage;
import cn.bctools.im.entity.enums.MessageTypeEnum;
import cn.bctools.im.mapper.ChatGroupMessageMapper;
import cn.bctools.im.message.dto.BaseSyncMessageDto;
import cn.bctools.im.service.ImMessageService;
import cn.bctools.im.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * @author ZhuXiaoKang
 * @Description 群聊全量消息服务实现
 */

@Slf4j
@Service
@AllArgsConstructor
public class ChatGroupMessageServiceImpl extends ServiceImpl<ChatGroupMessageMapper, ChatGroupMessage> implements ImMessageService {

    @Override
    public MessageTypeEnum getType() {
        return MessageTypeEnum.CHAT_GROUP;
    }

    @Override
    public void saveMessage(BaseSyncMessageDto syncMessageDto) {
        ChatGroupMessage chatGroupMessage = new ChatGroupMessage();
        BeanUtils.copyProperties(syncMessageDto, chatGroupMessage);
        chatGroupMessage.setCreateTime(DateUtil.millisecondToLocalDateTime(syncMessageDto.getCreateTime()));
        save(chatGroupMessage);
    }

    /**
     * 查询用户指定群组聊天历史记录, 按时间顺序排序
     *
     * @param reqDto
     * @return
     */
    @Override
    public Page<String> pageMessageHistory(HistoryMessageReqDto reqDto) {
        if (CollectionUtils.isEmpty(reqDto.getUserGroupIds()) || reqDto.getGroupId() == null ) {
            log.error("参数错误: {}", JSON.toJSONString(reqDto));
            return null;
        }

        // 判断用户是否绑定了目标群组，若未绑定，则认为是非法查询，直接返回空
        if (!reqDto.getUserGroupIds().contains(reqDto.getGroupId())) {
            log.error("查询历史聊天记录失败，用户{}未绑定的组id: {}", reqDto.getUserId(), reqDto.getGroupId());
            return null;
        }

        // 查询群聊历史消息
        LambdaQueryWrapper<ChatGroupMessage> queryWrapper = Wrappers.<ChatGroupMessage>lambdaQuery()
                .eq(ChatGroupMessage::getTenantId, reqDto.getTenantId())
                .eq(ChatGroupMessage::getGroupId, reqDto.getGroupId())
                .ge(ChatGroupMessage::getCreateTime, DateUtil.millisecondToLocalDateTime(reqDto.getBeginTime()));
        if (reqDto.getEndTime() != null) {
            queryWrapper.le(ChatGroupMessage::getCreateTime, DateUtil.millisecondToLocalDateTime(reqDto.getEndTime()));
        }
        queryWrapper
                .select(ChatGroupMessage::getMessage)
                .orderByDesc(ChatGroupMessage::getCreateTime);

        Page<ChatGroupMessage> page = new Page<>(reqDto.getCurrent(), reqDto.getSize());
        Page<ChatGroupMessage> resultMsg = page(page, queryWrapper);

        // 封装返回值
        Page<String> resDto = new Page<>();
        BeanUtils.copyProperties(resultMsg, resDto);
        resDto.setRecords(resultMsg.getRecords().stream().map(ChatGroupMessage::getMessage).collect(Collectors.toList()));
        return resDto;
    }
}
