package cn.bctools.log.annotation;

/**
 * @author guojing
 */
public interface LogCallBack {

    /**
     * 回调事件
     *
     * @param content     内容
     * @param createBy    创建人
     * @param description 描述
     */
    void callBack(String createBy, String description, String content);

}
