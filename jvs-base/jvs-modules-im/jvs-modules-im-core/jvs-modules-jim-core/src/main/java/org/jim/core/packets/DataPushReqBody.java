package org.jim.core.packets;

import com.alibaba.fastjson.JSONObject;

/**
 * @Author: ZhuXiaoKang
 * @Description: 数据推送cmd请求消息体
*/
public class DataPushReqBody extends Message {

    private static final long serialVersionUID = -2486442504830431394L;


    /**
     * 数据类型。 格式：服务名_业务名
     */
    private String type;

    /**
     * 推送的数据。 具体的数据格式由服务端自行定义
     */
    private JSONObject content;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JSONObject getContent() {
        return content;
    }

    public void setContent(JSONObject content) {
        this.content = content;
    }
}
