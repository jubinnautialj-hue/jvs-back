package cn.bctools.im.service.impl;

import cn.bctools.im.entity.ChannelData;
import cn.bctools.im.entity.ServerInfo;
import cn.bctools.im.service.ChannelDataStrategy;
import cn.bctools.im.service.ImChannelDataService;
import cn.bctools.im.service.ImServerInfoService;
import cn.bctools.im.service.ServerHeartbeatService;
import cn.bctools.redis.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jim.core.ImConst;
import org.jim.core.cluster.ImClusterVO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: ZhuXiaoKang
 * @Description: IM服务心跳检测
 */
@Slf4j
@Service
@AllArgsConstructor
public class ServerHeartbeatServiceImpl implements ServerHeartbeatService {

    /**
     * 锁时长(s)
     */
    private static final Integer LOCK_EXPIRE = 1000 * 60 * 60;
    private final ImServerInfoService imServerInfoService;
    private final ImChannelDataService imChannelDataService;
    private final ChannelDataStrategy channelDataStrategy;
    private final RedisUtils redisUtils;


    @Override
    public void sendHeartbeat() {
        imServerInfoService.update(Wrappers.<ServerInfo>lambdaUpdate()
                .set(ServerInfo::getHeartbeatTime, LocalDateTime.now())
                .eq(ServerInfo::getServerId, ImClusterVO.CLIENT_ID));
    }

    @Override
    public void checkHeartbeat(Integer heartbeatIntervalSecond, LocalDateTime checkTime) {
        // 得到已离线的服务id集合（心跳检测时间 - 服务最后一次心跳时间 > 心跳间隔）
        List<ServerInfo> serverInfos = imServerInfoService.list();
        List<String> offlineServerIds = serverInfos.stream()
                .filter(serverInfo -> ChronoUnit.SECONDS.between(serverInfo.getHeartbeatTime(), checkTime) > heartbeatIntervalSecond)
                .map(ServerInfo::getServerId)
                .collect(Collectors.toList());

        // 查询已离线服务是否有未处理的连接数据，若已处理完毕，则删除该服务信息
        if (CollectionUtils.isEmpty(offlineServerIds)) {
            return;
        }
        List<String> removeServerIds = new ArrayList<>();
        offlineServerIds.forEach(serverId -> {
            long dataTotal = imChannelDataService.count(Wrappers.<ChannelData>lambdaQuery().eq(ChannelData::getServerId, serverId));
            if (dataTotal == 0) {
                removeServerIds.add(serverId);
            }
        });
        if (CollectionUtils.isNotEmpty(removeServerIds)) {
            imServerInfoService.remove(Wrappers.<ServerInfo>lambdaQuery().in(ServerInfo::getServerId, removeServerIds));
        }

        // 处理已离线服务连接的数据
        offlineServerIds.removeAll(removeServerIds);
        processChannelData(offlineServerIds);
    }

    /**
     * 处理已离线服务连接的数据
     * @param serverIds 已离线的服务id集合
     */
    @Async
    public void processChannelData(List<String> serverIds) {
        for (String serverId : serverIds) {
            String lockKey = getLockKey(serverId);
            try {
                // 获取锁
                if (Boolean.FALSE.equals(redisUtils.tryLock(lockKey, LOCK_EXPIRE))) {
                    continue;
                }
                log.info("服务[{}]已停止, 处理该服务数据开始", serverId);
                // 处理服务连接数据
                processData(serverId);
                // 删除服务所有已处理的数据
                imChannelDataService.remove(Wrappers.<ChannelData>lambdaQuery()
                        .eq(ChannelData::getServerId, serverId)
                        .eq(ChannelData::getProcessStatus, Boolean.TRUE));
                log.info("服务[{}]已停止, 处理该服务数据结束", serverId);
            } finally {
                redisUtils.unLock(lockKey);
            }
        }
    }

    private String getLockKey(String serverId) {
        return new StringBuilder()
                .append(ImConst.JIM).append(":")
                .append("channelDataLock").append(":")
                .append(serverId)
                .toString();
    }

    /**
     * 处理服务连接数据
     * @param serverId 服务id
     */
    private void processData(String serverId) {
        // 每页处理数量
        final int size = 1000;
        // 页码
        int current = 0;
        Page<ChannelData> batchPage = new Page<>(current, size);
        // 分批处理服务连接数据
        while (current == 0 || batchPage.hasNext()) {
            // 分页查询
            current += 1;
            batchPage.setCurrent(current);
            batchPage = imChannelDataService.page(batchPage, Wrappers.<ChannelData>lambdaQuery()
                    .eq(ChannelData::getServerId, serverId)
                    .eq(ChannelData::getProcessStatus, Boolean.FALSE)
                    .orderByAsc(ChannelData::getId));
            List<ChannelData> channelDatas = batchPage.getRecords();
            if (CollectionUtils.isEmpty(channelDatas)) {
                break;
            }

            // 处理数据
            channelDatas.forEach(channelData -> {
                boolean processResult = channelDataStrategy.getService(channelData.getBusinessType()).process(channelData);
                if (processResult) {
                    channelData.setProcessStatus(Boolean.TRUE);
                }
            });
            // 修改处理状态
            imChannelDataService.updateBatchById(channelDatas);
        }
    }
}
