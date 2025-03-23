package cn.bctools.im.message.constant;

/**
 * @author ZhuXiaoKang
 * @Description Rabbit常量配置
 */
public class RabbitMqConstant {

    private RabbitMqConstant() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * IM消息持久化 - 交换机
     */
    public static final String EXCHANGE_SYNC_MESSAGE = "sync-message-exchange";

    /**
     * IM聊天消息持久化 - 路由
     */
    public static final String ROUTING_SYNC_CHAT = "sync-chat";

    /**
     * IM通知消息持久化 - 路由
     */
    public static final String ROUTING_SYNC_NOTIFY = "sync-notify";

    /**
     * IM聊天消息持久化 - 队列
     */
    public static final String QUEUE_SYNC_CHAT = ROUTING_SYNC_CHAT+ "-queue";

    /**
     * IM通知消息持久化 - 队列
     */
    public static final String QUEUE_SYNC_NOTIFY = ROUTING_SYNC_NOTIFY + "-queue";



}
