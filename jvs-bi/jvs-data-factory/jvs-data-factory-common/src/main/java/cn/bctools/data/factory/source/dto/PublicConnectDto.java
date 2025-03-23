package cn.bctools.data.factory.source.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 公共的连接信息 mysql,mongodb
 *
 * @author admin
 */
@Data
@Accessors(chain = true)
@ApiModel("mysql mongodb mariadb数据源入参")
public class PublicConnectDto {

    @ApiModelProperty("登录密码")
    private String sourcePwd;

    @ApiModelProperty("登录用户名")
    private String sourceUserName;

    @ApiModelProperty("ip地址")
    private String sourceHost;

    @ApiModelProperty("端口")
    private Integer sourcePort;

    @ApiModelProperty("库名")
    private String sourceLibraryName;

}
