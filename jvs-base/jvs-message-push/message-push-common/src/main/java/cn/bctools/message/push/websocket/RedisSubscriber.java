package cn.bctools.message.push.websocket;

import cn.bctools.message.push.entity.InsideNotice;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author xh
 */
@Slf4j
@Component
@AllArgsConstructor
public class RedisSubscriber {

    private final InsideNoticeEndPoint insideNoticeEndPoint;

    public void onMessage(String message, String pattern) {
        try {
            log.info("订阅消息：" + message);
            Object parse = JSON.parse(message);
            String s = parse.toString();
            InsideNotice insideNotice = JSONUtil.toBean(s, InsideNotice.class);
            insideNoticeEndPoint.sendMessageTo(insideNotice);
        } catch (Exception e) {
            log.error("订阅异常",e);
        }
    }
}
