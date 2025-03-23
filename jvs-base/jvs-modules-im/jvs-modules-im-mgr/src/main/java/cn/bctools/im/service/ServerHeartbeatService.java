package cn.bctools.im.service;

import java.time.LocalDateTime;

/**
 * @Author: ZhuXiaoKang
 * @Description: IM服务心跳检测
 */
public interface ServerHeartbeatService {

    /**
     * 发送服务心跳
     */
    void sendHeartbeat();

    /**
     * 检测所有服务心跳
     * @param heartbeatIntervalSecond 心跳间隔时长
     * @param checkTime 检测时间
     */
    void checkHeartbeat(Integer heartbeatIntervalSecond, LocalDateTime checkTime);
}
