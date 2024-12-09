package cn.bctools.message.push.dto.messagepush.dingtalk.robot;

import cn.bctools.message.push.dto.messagepush.BaseMessage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 钉钉群消息FeedCard类型DTO
 *
 *
 * @author xh*/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@ApiModel("钉钉群消息FeedCard类型DTO")
public class FeedCardMessageDTO extends BaseMessage {
    private static final long serialVersionUID = -3289428483627765265L;

    @ApiModelProperty("是否@所有人")
    private Boolean all;


    @ApiModelProperty("多条信息设置")
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        @ApiModelProperty("单条信息文本")
        private String title;
        @ApiModelProperty("点击单条信息的链接")
        private String messageUrl;
        @ApiModelProperty("单条信息图片的URL")
        private String picUrl;
    }

}
