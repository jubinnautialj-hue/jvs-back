package cn.bctools.im.dto.history;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ZhuXiaoKang
 * @Description 历史消息(私聊|群聊|通知)查询入参
 */

@Data
public class HistoryMessageReqDto {

    @NotNull
    @ApiModelProperty(value = "租户id")
    private String tenantId;

    @NotNull
    @ApiModelProperty(value = "用户id", required = true)
    private String userId;

    @NotNull
    @ApiModelProperty(value = "查询范围开始时间戳", required = true)
    private Long beginTime;

    @ApiModelProperty(value = "查询范围结束时间戳", notes = "为空，则查询开始日期之后的所有数据")
    private Long endTime;

    @ApiModelProperty(value = "用户所属组id集合")
    private List<String> userGroupIds;

    @ApiModelProperty(value = "目标组id")
    private String groupId;

    @ApiModelProperty(value = "消息发送人id", notes = "查询指定用户私聊历史聊天记录")
    private String fromUserId;

    @NotNull
    @ApiModelProperty(value = "页码")
    private Long current;

    @NotNull
    @ApiModelProperty(value = "每页数量")
    private Long size;
}
