package cn.bctools.im.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author ZhuXiaoKang
 * @Description 私聊全量消息表
 */

@Data
@Accessors(chain = true)
@TableName(value = "im_chat_message")
public class ChatMessage extends BaseEntity {

    @ApiModelProperty(value = "发送消息用户id")
    private String fromUserId;
    @ApiModelProperty(value = "接收消息用户id")
    private String toUserId;
    @ApiModelProperty(value = "聊天消息体")
    private String message;
}
