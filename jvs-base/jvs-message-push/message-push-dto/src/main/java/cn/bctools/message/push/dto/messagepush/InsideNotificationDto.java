package cn.bctools.message.push.dto.messagepush;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
/**
 * @author czy
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@ApiModel("站内推送数据")
public class InsideNotificationDto extends BaseMessage {
    private static final long serialVersionUID = 2584119180794722634L;

    @ApiModelProperty("消息批次号")
    private String requestNo;

    @ApiModelProperty("消息内容")
    private String content;


    @ApiModelProperty("消息子类")
    private String subClass = "通知消息";

    @ApiModelProperty("消息回调地址")
    private String callBackUrl;
}
