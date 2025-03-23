package cn.bctools.im.service;

import cn.bctools.common.exception.BusinessException;
import org.jim.core.packets.message.MessageReqTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息查询策略
 */
@Component
public class SearchMessageStrategy {

    private Map<String, SearchMessageService> messageServiceMap = new HashMap<>();

    @Autowired
    public SearchMessageStrategy(List<SearchMessageService> messageServices) {
        messageServices.forEach(messageService -> messageServiceMap.put(messageService.getType(), messageService));
    }

    public SearchMessageService getMessageService(Integer type) {
        MessageReqTypeEnum typeEnum = MessageReqTypeEnum.getByValue(type);
        if (typeEnum == null) {
            throw new BusinessException("消息查询失败，不支持的消息类型：{}", type);
        }
        SearchMessageService messageService = messageServiceMap.entrySet().stream()
                .filter(e -> e.getKey().contains(typeEnum.name()))
                .findFirst()
                .get()
                .getValue();
        if (messageService == null) {
            throw new BusinessException("消息查询失败，未实现的消息类型：{}", type);
        }
        return messageService;
    }
}
