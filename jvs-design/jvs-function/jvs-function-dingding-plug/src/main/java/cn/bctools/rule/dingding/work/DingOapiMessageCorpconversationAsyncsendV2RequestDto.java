package cn.bctools.rule.dingding.work;

import cn.bctools.rule.annotations.ParameterValue;
import cn.bctools.rule.entity.enums.InputType;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author jvs
 * The type Ding oapi message corpconversation asyncsend v 2 request dto.
 */
@Data
@Accessors(chain = true)
public class DingOapiMessageCorpconversationAsyncsendV2RequestDto {

    /**
     * The Users.
     */
    @ParameterValue(info = "用户对象", type = InputType.userList, explain = "获取多个用户,需要参数为用户id值")
    public List<String> users;

    /**
     * The Msg type.
     */
    @ParameterValue(info = "消息类型。", type = InputType.selected, cls = DingMsgTypeSelected.class)
    public DingMsgType msgType;

    /**
     * The Media id.
     */
    @ParameterValue(info = "资源标识", necessity = false, type = InputType.selected, cls = DingMsMediaIdSelected.class)
    public String mediaId;

    /**
     * The Content.
     */
    @ParameterValue(info = "消息内容，最长不超过2048个字节。", necessity = false, type = InputType.text)
    public String content;


    /**
     * markdown
     */
    @ParameterValue(info = "标题", necessity = false, type = InputType.text)
    public String title;
    /**
     * The Text.
     */
    @ParameterValue(info = "makdown内容", necessity = false, type = InputType.longtext)
    public String text;
    /**
     * link
     */
    @ParameterValue(info = "图片地址", necessity = false, type = InputType.text)
    public String picUrl;
    /**
     * The Message url.
     */
    @ParameterValue(info = "点击后跳转地址", necessity = false, type = InputType.text)
    public String messageUrl;


}
