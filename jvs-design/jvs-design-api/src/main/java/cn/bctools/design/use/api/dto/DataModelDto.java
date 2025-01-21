package cn.bctools.design.use.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class DataModelDto {

    @ApiModelProperty("表的标识，跨环境不变的")
    private String tableCode;

    @ApiModelProperty("应用标识")
    private String appCode;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("表名称")
    private String tableName;

    @ApiModelProperty("表-解释")
    private String tableNameDesc;

}
