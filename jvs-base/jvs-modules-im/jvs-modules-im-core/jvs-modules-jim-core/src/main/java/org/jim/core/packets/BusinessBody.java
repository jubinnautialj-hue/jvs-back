package org.jim.core.packets;

import com.alibaba.fastjson.JSONObject;

/**
 * @Author: ZhuXiaoKang
 * @Description: 业务自定义消息体
*/
public class BusinessBody extends Message {

    private static final long serialVersionUID = -3561997626705139110L;

    /**
     * 业务自定义消息唯一id
     */
    private String businessId;

    /**
     * 用户id
     */
    private String from;

    /**
     * 消息有效时长(秒) 必传
     */
    private Long expire;

    /**
     * 内容（JSON）
     */
    private JSONObject content;

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getExpire() {
        return expire;
    }

    public void setExpire(Long expire) {
        this.expire = expire;
    }

    public JSONObject getContent() {
        return content;
    }

    public void setContent(JSONObject content) {
        this.content = content;
    }
}
