package cn.bctools.message.push.dto.messagepush.wechatwork.agent;

import cn.bctools.message.push.dto.messagepush.BaseMessage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 企业微信Markdown
 *
 *
 * @author xh*/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("企业微信Markdown")
public class MarkdownMessageDTO extends BaseMessage {
    private static final long serialVersionUID = 8123659270032033936L;

    @ApiModelProperty( "PartyID列表，非必填，多个接受者用‘|’分隔。当touser为@all时忽略本参数")
    private String toParty;

    @ApiModelProperty("TagID列表，非必填，多个接受者用‘|’分隔。当touser为@all时忽略本参数")
    private String toTag;

    @ApiModelProperty("请输入Markdown内容...")
    private String content;


}
