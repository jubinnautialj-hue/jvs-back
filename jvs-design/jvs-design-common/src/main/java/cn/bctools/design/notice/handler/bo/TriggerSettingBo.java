package cn.bctools.design.notice.handler.bo;

import cn.bctools.design.notice.handler.enums.TriggerTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 消息通知触发设置
 */
@Data
@ApiModel("消息通知触发设置")
public class TriggerSettingBo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "触发类型", required = true)
    @NotNull(message = "请选择触发类型")
    private TriggerTypeEnum type;

    @ApiModelProperty(value = "触发条件JSON字符串")
    private String condition;
}
