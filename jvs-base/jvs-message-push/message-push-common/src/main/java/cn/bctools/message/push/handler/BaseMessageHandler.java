package cn.bctools.message.push.handler;

import cn.bctools.message.push.dto.messagepush.BaseMessage;

/**
 * 消息处理器基类
 *
 * @author xh
 */
public abstract class BaseMessageHandler<T extends BaseMessage> {

    /**
     * 实现这个接口来处理消息，再正式调用这个方法之前会处理好需要的参数和需要的配置
     *
     * @param param 处理消息参数
     * @throws Exception
     */
    public abstract void handle(T param) throws Exception;

    /**
     * 实现这个接口来处理消息，再正式调用这个方法之前会处理好需要的参数和需要的配置
     *
     * @param pushHisId 重发数据消息
     * @throws Exception
     */
    public abstract void resend(String pushHisId) throws Exception;

}
