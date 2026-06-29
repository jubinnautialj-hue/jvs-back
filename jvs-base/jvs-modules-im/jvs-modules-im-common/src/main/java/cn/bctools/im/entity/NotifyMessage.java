package cn.bctools.im.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author ZhuXiaoKang
 * @Description IM通知全量消息表
 */

@Data
@Accessors(chain = true)
@TableName(value = "im_notify_message", autoResultMap = true)
public class NotifyMessage extends BaseEntity {

    @ApiModelProperty(value = "0-广播，1-组通知，2-精确通知")
    private Integer notifyType;

    @ApiModelProperty(value = "发送消息用户id(若是系统发送，则默认为0)")
    private String fromUserId;

    @ApiModelProperty(value = "目标群组id(JSON数组)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> groupIds;

    @ApiModelProperty(value = "目标用户id(JSON数组)")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> userIds;

    @ApiModelProperty(value = "聊天消息体")
    private String message;

}
