package cn.bctools.redis;

import cn.bctools.common.utils.ObjectNull;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author guojing
 */
public class JvsMessageListenerAdapter extends MessageListenerAdapter {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String msg = new String(message.getBody());
        if (ObjectNull.isNull(msg)) {
            onMessage(msg);
        } else {
            onMessage(JSON.parse(new String(message.getBody())).toString());
        }
    }

    public void onMessage(String s) {

    }
}
