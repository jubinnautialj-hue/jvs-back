package cn.bctools.design.project.dto;

import cn.bctools.common.entity.dto.UserDto;
import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("模式切换入参")
public class SwitchModeDto {

    @ApiModelProperty(value = "模式", required = true)
    @NotNull(message = "请选择模式")
    private AppVersionTypeEnum mode;
    @ApiModelProperty(value = "模拟用户")
    private UserDto analogUser;
}
