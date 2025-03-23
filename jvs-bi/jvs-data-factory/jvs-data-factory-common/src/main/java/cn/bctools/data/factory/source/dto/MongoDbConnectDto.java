package cn.bctools.data.factory.source.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("mongodb 数据源入参")
public class MongoDbConnectDto extends PublicConnectDto {
    @ApiModelProperty("用户密码认证的库")
    private String authenticationDatabase;
}
