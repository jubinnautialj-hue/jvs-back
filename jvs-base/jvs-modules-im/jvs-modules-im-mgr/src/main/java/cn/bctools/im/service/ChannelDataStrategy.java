package cn.bctools.im.service;

import cn.bctools.common.exception.BusinessException;
import cn.bctools.im.entity.ChannelData;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jim.core.enums.ChannelDataBusinessTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: 服务连接相关消息处理策略
 */
@Component
public class ChannelDataStrategy {

    private Map<String, ChannelDataService> channelDataServiceMap;

    @Autowired
    public ChannelDataStrategy(List<ChannelDataService> channelDataServiceMap) {
        this.channelDataServiceMap = channelDataServiceMap.stream().collect(Collectors.toMap(ChannelDataService::getType, Function.identity()));
    }

    /**
     * 获取服务
     * @param type
     */
    public ChannelDataService getService(String type) {
        ChannelDataBusinessTypeEnum typeEnum = ChannelDataBusinessTypeEnum.getByValue(type);
        if (typeEnum == null) {
            throw new BusinessException("服务连接相关消息处理策略失败，不支持的业务类型：{}", type);
        }
        ChannelDataService channelDataService = channelDataServiceMap.entrySet().stream()
                .filter(e -> e.getKey().equals(type))
                .findFirst()
                .get()
                .getValue();
        if (channelDataService == null) {
            throw new BusinessException("服务连接相关消息处理策略失败，未实现的业务类型：{}", type);
        }
        return channelDataService;
    }
}
