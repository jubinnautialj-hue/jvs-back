package cn.bctools.message.push.dto.messagepush.wechatofficialaccount;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 微信模板Data
 *
 * @author xh
 */
@Data
@Accessors(chain = true)
@ApiModel("按钮消息")
public class WechatTemplateData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "值")
    private String value;
    @ApiModelProperty(value = "显示颜色")
    private String color;

}
