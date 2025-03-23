package cn.bctools.data.factory.notice.dto;

import cn.bctools.data.factory.notice.bo.TriggerSettingBo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author: ZhuXiaoKang
 * @Description: 消息通知
 */
@Data
@Accessors(chain = true)
@ApiModel("消息通知")
public class NoticeDto {
    @ApiModelProperty(value = "消息通知id")
    private String id;

    @ApiModelProperty(value = "false-禁用, true-启用", required = true)
    @NotNull(message = "请选择是否启用")
    private Boolean enabled;

    @ApiModelProperty(value = "模板名称", required = true)
    @NotBlank(message = "模板名称不能为空")
    private String name;

    @ApiModelProperty(value = "触发设置", required = true)
    @NotNull(message = "请配置触发条件")
    private TriggerSettingBo triggerSetting;

    @ApiModelProperty("模板扩展配置")
    List<NoticeExtendTemplateDto> extend;
}
