package cn.bctools.im.strategy;

import cn.bctools.im.service.ImMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhuXiaoKang
 * @Description 聊天消息同步入库策略类
 */

@Slf4j
@Component
public class SyncMessageStrategy {

    private Map<Integer, ImMessageService> imMessageServiceMap = new ConcurrentHashMap<>();

    @Autowired
    public SyncMessageStrategy(List<ImMessageService> imMessageServices) {
        imMessageServices.forEach(ims ->
            imMessageServiceMap.put(ims.getType().getValue(), ims)
        );
    }

    /**
     * 得到处理消息的具体实现
     * @param type
     * @return
     */
    public ImMessageService getImMessageService(Integer type) {
        if (type == null) {
            log.error("参数错误,消息类型为空");
            return null;
        }
        ImMessageService service = imMessageServiceMap.get(type);
        if (service == null) {
            log.error("未知的消息类型: {}", type);
        }
        return service;
    }
}
