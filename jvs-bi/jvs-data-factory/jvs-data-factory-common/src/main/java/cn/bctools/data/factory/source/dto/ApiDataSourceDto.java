package cn.bctools.data.factory.source.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * api数据源配置信息
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("api数据源入参")
public class ApiDataSourceDto {
    @ApiModelProperty("认证权限")
    private String authJson;
    @ApiModelProperty("jar包id")
    private String sysJarId;
}
