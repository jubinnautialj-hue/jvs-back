package cn.bctools.im.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author ZhuXiaoKang
 * @Description 群聊全量消息表
 */

@Data
@Accessors(chain = true)
@TableName(value = "im_chat_group_message")
public class ChatGroupMessage extends BaseEntity{

    @ApiModelProperty(value = "群组id")
    private String groupId;
    @ApiModelProperty(value = "发送消息用户id")
    private String fromUserId;
    @ApiModelProperty(value = "聊天消息体")
    private String message;
}
