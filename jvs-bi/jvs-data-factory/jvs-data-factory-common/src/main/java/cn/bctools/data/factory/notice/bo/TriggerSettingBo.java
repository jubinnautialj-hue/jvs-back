package cn.bctools.data.factory.notice.bo;

import cn.bctools.data.factory.notice.enums.TriggerTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
@ApiModel("消息通知触发设置")
public class TriggerSettingBo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "触发类型", required = true)
    @NotNull(message = "请选择触发类型")
    private TriggerTypeEnum type;

}
