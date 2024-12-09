package cn.bctools.rule.cons;

/**
 * 目前此样式预览功能未启用
 *
 * @author wl
 */
public enum SHEnum {
    /**
     * 卡片
     */
    kap("卡片"),
    /**
     * 图文选择
     */
    imgTextSelected("图文选择"),
    /**
     * 图片选择
     */
    imgSelected("图片选择"),
    /**
     * 文本消息预览
     */
    textMessage("文本消息预览"),
    /**
     * 图文消息预览
     */
    imgMessage("图文消息预览"),
    /**
     * 前端成功失败
     */
    vueMessageError("前端成功失败"),
    /**
     * 前端成功提示
     */
    vueMessageSuccess("前端成功提示");

    String msg;

    SHEnum(String msg) {
        this.msg = msg;
    }

}
