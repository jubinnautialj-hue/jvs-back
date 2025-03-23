package cn.bctools.im.service.impl;

import cn.bctools.im.entity.ChannelData;
import cn.bctools.im.service.ChannelDataService;
import cn.bctools.im.service.ImChannelDataService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jim.core.cache.redis.JedisTemplate;
import org.jim.core.enums.ChannelDataBusinessTypeEnum;
import org.jim.core.enums.ChannelDataStoreTypeEnum;
import org.springframework.stereotype.Service;

/**
 * @Author: ZhuXiaoKang
 * @Description: 组用户信息hash处理
 */
@Slf4j
@Service
@AllArgsConstructor
public class ChannelDataGroupUserInfoServiceImpl implements ChannelDataService {

    private final ImChannelDataService imChannelDataService;

    @Override
    public String getType() {
        return ChannelDataBusinessTypeEnum.GROUP_USER_INFO.getValue();
    }

    @Override
    public void save(ChannelData channelData, Object data) {
        // 设置非当前服务连接的对应数据为已处理
        imChannelDataService.update(Wrappers.<ChannelData>lambdaUpdate()
                .set(ChannelData::getProcessStatus, Boolean.TRUE)
                .eq(ChannelData::getUserId, channelData.getUserId())
                .eq(ChannelData::getStoreCode, channelData.getStoreCode())
                .ne(ChannelData::getServerId, channelData.getServerId()));
        // 保存
        imChannelDataService.saveOrUpdate(channelData, Wrappers.<ChannelData>lambdaQuery()
                .eq(ChannelData::getUserId, channelData.getUserId())
                .eq(ChannelData::getStoreCode, channelData.getStoreCode())
                .eq(ChannelData::getServerId, channelData.getServerId()));
    }

    @Override
    public Boolean process(ChannelData channelData) {
        try {
            // redis存储处理
            if (ChannelDataStoreTypeEnum.REDIS.getValue().equals(channelData.getStoreType())) {
                JedisTemplate.me().hDelete(channelData.getStoreCode(), channelData.getUserId());
            }
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("组内用户信息:{},处理异常: {}", channelData.getUserId(), e.getMessage());
        }
        return Boolean.FALSE;
    }

    @Override
    public void delete(String serverId, String userId, String storeCode) {
        imChannelDataService.remove(Wrappers.<ChannelData>lambdaQuery()
                .eq(ChannelData::getServerId, serverId)
                .eq(ChannelData::getUserId, userId)
                .eq(ChannelData::getStoreCode, storeCode));
    }
}
