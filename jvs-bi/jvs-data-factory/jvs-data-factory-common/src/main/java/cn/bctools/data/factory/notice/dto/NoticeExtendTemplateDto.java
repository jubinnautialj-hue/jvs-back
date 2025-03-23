package cn.bctools.data.factory.notice.dto;

import cn.bctools.data.factory.notice.enums.DingNoticeEnum;
import cn.bctools.data.factory.notice.enums.NoticeTypeEnum;
import cn.bctools.data.factory.notice.enums.WxEnterpriseNoticeEnum;
import cn.bctools.message.push.dto.messagepush.wechatofficialaccount.TemplateMessageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息模板扩展配置
 */
@Data
@Accessors(chain = true)
@ApiModel("消息模板扩展配置")
public class NoticeExtendTemplateDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("消息类型")
    private NoticeTypeEnum type;

    @ApiModelProperty(value = "标题")
    private String titleHtml;

    @ApiModelProperty(value = "内容")
    private String contentHtml;

    @ApiModelProperty(value = "其它平台消息模板")
    private String template;

    @ApiModelProperty(value = "消息模板code")
    private String templateCode;

    @ApiModelProperty(value = "变量")
    private List<VariableDto> variables;

    @ApiModelProperty(value = "微信公众号模板消息")
    private TemplateMessageDTO wechatMp;

    @ApiModelProperty(value = "企业微信模板消息")
    private Map<WxEnterpriseNoticeEnum, Object> wxEnterprise;

    @ApiModelProperty(value = "钉钉模板消息")
    private Map<DingNoticeEnum, Object> ding;


}
