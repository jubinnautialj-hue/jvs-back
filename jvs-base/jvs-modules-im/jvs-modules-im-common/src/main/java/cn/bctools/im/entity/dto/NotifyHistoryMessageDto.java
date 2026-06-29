package cn.bctools.im.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ZhuXiaoKang
 * @Description 通知查询条件
 */
@Data
public class NotifyHistoryMessageDto {

    @ApiModelProperty(value = "开始时间")
    private LocalDateTime beginTime;

    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "租户")
    private String tenantId;

    @ApiModelProperty(value = "群组id集合")
    private List<String> groupIds;

    @ApiModelProperty(value = "用户id")
    private String userId;
}
