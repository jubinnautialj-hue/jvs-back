package cn.bctools.design.project.dto;

import cn.bctools.design.project.entity.enums.AppVersionTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("切换版本入参")
public class AppVersionSwitchDto {

    @ApiModelProperty("版本id")
    private String versionId;

    @ApiModelProperty("版本类型")
    private AppVersionTypeEnum versionType;
}
