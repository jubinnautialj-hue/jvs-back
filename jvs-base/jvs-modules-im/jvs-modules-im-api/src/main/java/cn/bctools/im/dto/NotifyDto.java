package cn.bctools.im.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 通知DTO
 */

@Data
@Accessors(chain = true)
@ApiModel("通知")
public class NotifyDto {

    @ApiModelProperty(value = "业务类型", required = true)
    @NotNull(message = "业务类型不能为空")
    private String businessType;

    @ApiModelProperty(value = "发送消息时间戳", required = true)
    @NotNull(message = "发送消息时间戳不能为空")
    protected Long createTime;

    @ApiModelProperty(value = "通知类型 (0:广播、1:组通知、2:精确通知)", required = true)
    @NotNull(message = "通知类型不能为空")
    private Integer notifyType;

    @ApiModelProperty(value = "组通知id集合 (notifyType为\"组通知\"时必传)")
    private List<String> toGroupIds;

    @ApiModelProperty(value = "目标用户id集合 (notifyType为\"精确通知\"时必传)")
    private List<String> toUserIds;

    @ApiModelProperty(value = "目标租户id集合（notifyType为“广播”时必传）")
    private List<String> tenantIds;
    @ApiModelProperty(value = "当前租户id", required = true)
    @NotNull(message = "租户id不能为空")
    private String tenantId;

    @ApiModelProperty(value = "发送用户id（0-表示系统发送）", required = true)
    @NotNull(message = "发送用户id不能为空")
    private String from;

    @ApiModelProperty(value = "标题", required = true)
    @NotNull(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "消息内容", required = true)
    @NotNull(message = "消息内容不能为空")
    private JSONObject content;
}
