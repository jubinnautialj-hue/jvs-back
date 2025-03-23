package cn.bctools.im.task;

import cn.bctools.im.service.ServerHeartbeatService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author: ZhuXiaoKang
 * @Description: 定时发送IM服务的心跳
 */
@Component
@AllArgsConstructor
public class ServerScheduled {

    private final ServerHeartbeatService serverHeartbeatService;

    /**
     * 服务心跳检测间隔
     */
    private static final Integer HEARTBEAT_INTERVAL_SECOND = 30;
    private static final String CHECK_HEARTBEAT_CRON = "0/30 * * * * ?";
    /**
     * 服务发送心跳间隔
     */
    private static final String HEART_BEAT_CRON = "0/5 * * * * ?";

    @Scheduled(cron = HEART_BEAT_CRON)
    public void heartbeat() {
        serverHeartbeatService.sendHeartbeat();
    }

    @Scheduled(cron = CHECK_HEARTBEAT_CRON)
    public void checkHeartbeat() {
        serverHeartbeatService.checkHeartbeat(HEARTBEAT_INTERVAL_SECOND, LocalDateTime.now());
    }

}
