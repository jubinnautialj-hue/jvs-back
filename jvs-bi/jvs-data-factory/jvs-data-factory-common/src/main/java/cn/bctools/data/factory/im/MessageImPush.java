package cn.bctools.data.factory.im;


import cn.bctools.data.factory.enums.ImPushTypeEnums;

/**
 * im消息通知
 *
 * @author xiaohui
 */
public interface MessageImPush {
    /**
     * 消息通知
     *
     * @param type  类型
     * @param value 值
     * @param id    数据id
     */
    void notify(ImPushTypeEnums type, String value, String id);

    /**
     * 消息通知
     *
     * @param value        值
     * @param businessType 通知的组
     */
    void notify(Object value, String businessType);

    /**
     * 获取im组id
     *
     * @param tenantId 租户id
     */
    String getGroupId(String tenantId);
}
