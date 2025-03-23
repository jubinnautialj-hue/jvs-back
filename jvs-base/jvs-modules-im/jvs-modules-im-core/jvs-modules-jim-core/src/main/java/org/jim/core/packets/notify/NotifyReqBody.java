package org.jim.core.packets.notify;

import org.jim.core.packets.Message;

import java.util.List;

/**
 * @author ZhuXiaoKang
 * @Description 消息推送cmd请求消息体
 */
public class NotifyReqBody extends Message {

    private static final long serialVersionUID = 7976524573465866656L;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 通知类型 (0:广播、1:组通知、2:精确通知)
     */
    private Integer notifyType;

    /**
     * 组通知id集合 (notifyType为"组通知"时必传)
     */
    private List<String> toGroupIds;

    /**
     * 目标用户id集合 (notifyType为"精确通知"时必传)
     */
    private List<String> toUserIds;

    /**
     * 目标租户id集合（notifyType为“广播”时必传）
     */
    private List<String> tenantIds;

    /**
     * 发送用户id（不传或为0，表示系统发送）;
     */
    private String from;

    /**
     * 标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;


    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Integer getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(Integer notifyType) {
        this.notifyType = notifyType;
    }

    public List<String> getToGroupIds() {
        return toGroupIds;
    }

    public void setToGroupIds(List<String> toGroupIds) {
        this.toGroupIds = toGroupIds;
    }

    public List<String> getToUserIds() {
        return toUserIds;
    }

    public void setToUserIds(List<String> toUserIds) {
        this.toUserIds = toUserIds;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTenantIds() {
        return tenantIds;
    }

    public void setTenantIds(List<String> tenantIds) {
        this.tenantIds = tenantIds;
    }
}
