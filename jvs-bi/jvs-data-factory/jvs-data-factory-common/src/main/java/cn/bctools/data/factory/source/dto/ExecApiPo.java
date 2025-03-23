package cn.bctools.data.factory.source.dto;


import cn.bctools.data.factory.source.entity.DataSourceStructure;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
@ApiModel("用户设计阶段制作api入参")
public class ExecApiPo extends DataSourceStructure {

    @ApiModelProperty("authJson认证json")
    private String authJson;

    @ApiModelProperty("jar包id")
    private String sysJarId;
}
