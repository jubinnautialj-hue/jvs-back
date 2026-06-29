package cn.bctools.im.service;

import cn.bctools.im.entity.ChannelData;

/**
 * @Author: ZhuXiaoKang
 * @Description: 服务连接相关消息处理接口
 */
public interface ChannelDataService {

    /**
     * 业务类型
     * @return
     */
    String getType();

    /**
     * 保存
     * @param channelData 连接消息
     * @param data 消息
     */
    void save(ChannelData channelData, Object data);

    /**
     * 处理已离线服务的连接数据
     * @param channelData 连接消息
     * @return TRUE-处理成功，FALSE-处理失败
     */
    Boolean process(ChannelData channelData);

    /**
     * 删除数据
     * @param serverId 服务id
     * @param userId 用户id
     * @param storeCode 存储唯一编号
     */
    void delete(String serverId, String userId, String storeCode);

}
