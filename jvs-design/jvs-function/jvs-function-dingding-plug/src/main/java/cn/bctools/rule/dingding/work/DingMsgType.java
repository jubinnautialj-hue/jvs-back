package cn.bctools.rule.dingding.work;

/**
 * The enum Ding msg type.
 *
 * @author jvs
 */
public enum DingMsgType {
    /**
     * 文本消息 ding msg type.
     */
    文本消息("text"),
    /**
     * 图片消息 ding msg type.
     */
    图片消息("image"),
    /**
     * 文件消息 ding msg type.
     */
//    语音消息(""),
    文件消息("file"),
    /**
     * 链接消息 ding msg type.
     */
    链接消息("link"),
    /**
     * Markdown 消息 ding msg type.
     */
    Markdown消息("markdown"),
    /**
     * 卡片消息 ding msg type.
     */
    卡片消息("action_card");

    /**
     * The Value.
     */
    public final String value;

    DingMsgType(String value) {
        this.value = value;
    }
}
