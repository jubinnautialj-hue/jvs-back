package cn.bctools.message.push.dto.messagepush;

import cn.bctools.message.push.dto.enums.InsideNotificationTypeEnum;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author xh
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@ApiModel("消息基础配置")
public class BaseMessage implements Serializable {

    private static final long serialVersionUID = 3583298470031427965L;
    /**
     * 指定clientId发送
     */
    @ApiModelProperty("客户端唯一标识")
    private String clientCode;

    @ApiModelProperty("自定义接收人 邮件-邮件地址 短信-电话号码等")
    private List<ReceiversDto> definedReceivers;

    public boolean hasReceiver() {
        return this.definedReceivers != null && !this.definedReceivers.isEmpty();
    }

    /**
     * 值为 通知 项目
     * 不传的话默认为 通知类型
     */
    @ApiModelProperty("消息通知大类")
    private InsideNotificationTypeEnum largeCategories = InsideNotificationTypeEnum.notice;

    private String createById;
    /**
     * 发送消息,可能是租户logo,也可以传递用户头像
     */
    private String logo;
    /**
     * 租户id
     */
    private String tenantId;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 修改人
     */
    private String updateBy;
}
