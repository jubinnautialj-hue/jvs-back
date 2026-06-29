package cn.bctools.im.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bctools.im.dto.history.HistoryMessageReqDto;
import cn.bctools.im.entity.NotifyMessage;
import cn.bctools.im.entity.dto.NotifyHistoryMessageDto;
import cn.bctools.im.entity.enums.MessageTypeEnum;
import cn.bctools.im.mapper.NotifyMessageMapper;
import cn.bctools.im.message.dto.BaseSyncMessageDto;
import cn.bctools.im.message.dto.SyncNotifyMessageDto;
import cn.bctools.im.service.ImMessageService;
import cn.bctools.im.utils.DateUtil;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZhuXiaoKang
 * @Description IM通知全量消息服务实现
 */

@Service
@AllArgsConstructor
public class NotifyMessageServiceImpl extends ServiceImpl<NotifyMessageMapper, NotifyMessage> implements ImMessageService {

    @Override
    public MessageTypeEnum getType() {
        return MessageTypeEnum.NOTIFY;
    }

    @Override
    public void saveMessage(BaseSyncMessageDto syncMessageDto) {
        SyncNotifyMessageDto syncDto = (SyncNotifyMessageDto)syncMessageDto;
        NotifyMessage notifyMessage = new NotifyMessage();
        BeanUtils.copyProperties(syncDto, notifyMessage);
        notifyMessage.setCreateTime(DateUtil.millisecondToLocalDateTime(syncMessageDto.getCreateTime()));
        notifyMessage.setGroupIds(JSON.parseArray(syncDto.getGroupIds(), String.class));
        notifyMessage.setUserIds(JSON.parseArray(syncDto.getUserIds(), String.class));

        // 若租户id集合为空，则直接保存
        List<String> broadcastTenantIds =  JSON.parseArray(syncDto.getTenantIds(), String.class);
        if(CollectionUtils.isEmpty(broadcastTenantIds)) {
            save(notifyMessage);
            return;
        }

        // 若租户id集合不为空，则表示为广播。 按租户插入数据
        broadcastTenantIds.stream().forEach(tenantId -> {
            notifyMessage.setTenantId(tenantId);
            save(notifyMessage);
        });


    }

    /**
     * 查询指定用户所有通知历史消息
     *
     * @param reqDto
     * @return
     */
    @Override
    public Page<String> pageMessageHistory(HistoryMessageReqDto reqDto) {
        // 查询通知历史消息
        Page<NotifyMessage> page = new Page<>(reqDto.getCurrent(), reqDto.getSize());

        NotifyHistoryMessageDto notifyHistoryMessageDto = new NotifyHistoryMessageDto();
        notifyHistoryMessageDto.setTenantId(reqDto.getTenantId());
        notifyHistoryMessageDto.setBeginTime(DateUtil.millisecondToLocalDateTime(reqDto.getBeginTime()));
        notifyHistoryMessageDto.setEndTime(reqDto.getEndTime() != null ? DateUtil.millisecondToLocalDateTime(reqDto.getEndTime()) : null);
        notifyHistoryMessageDto.setGroupIds(reqDto.getUserGroupIds());
        notifyHistoryMessageDto.setUserId(reqDto.getUserId());
        Page<NotifyMessage> resultMsg = baseMapper.pageUserHistory(page, notifyHistoryMessageDto);

        // 封装返回值
        Page<String> resDto = new Page<>();
        BeanUtils.copyProperties(resultMsg, resDto);
        resDto.setRecords(resultMsg.getRecords().stream().map(NotifyMessage::getMessage).collect(Collectors.toList()));
        return resDto;
    }
}
