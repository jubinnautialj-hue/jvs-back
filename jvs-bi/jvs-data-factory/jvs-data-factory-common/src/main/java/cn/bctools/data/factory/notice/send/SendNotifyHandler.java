package cn.bctools.data.factory.notice.send;

import cn.bctools.data.factory.notice.dto.DataNotifyDto;

/**
 * @Author: ZhuXiaoKang
 * @Description: 发送消息通知
 */
public interface SendNotifyHandler {


    /**
     * 节点类型
     *
     * @return
     */
    String getType();


    /**
     * 发送消息通知
     *
     * @param dto
     */
    void send(DataNotifyDto dto);


}
