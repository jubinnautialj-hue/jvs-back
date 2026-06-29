package cn.bctools.im.utils;

/**
 * @Author: ZhuXiaoKang
 * @Description: 租户id工具类
 */
public class ChannelTenantUtil {

    /**
     * 租户id
     * @param channelTenantId 通道中的租户id（系统发送的情况(未登录), 默认租户id为-1， 需要使用内容中的租户id）
     * @param bodyTenantId 消息内容中的租户id
     * @return
     */
    public static String build(String channelTenantId, String bodyTenantId) {
        return channelTenantId.equals("-1") ? bodyTenantId : channelTenantId;
    }
}
