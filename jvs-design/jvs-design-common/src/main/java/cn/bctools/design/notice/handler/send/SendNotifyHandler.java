package cn.bctools.design.notice.handler.send;

/**
 * @author zhuxiaokang
 * 发送消息通知
 */
public interface SendNotifyHandler {


    /**
     * 节点类型
     * @return
     */
    String getType();


    /**
     * 发送消息通知
     * @param dto
     */
    void send(DataNotifyDto dto);

}
