package cn.bctools.data.factory.source.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class KingBaseConnectDto {

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

    @ApiModelProperty("模式名称")
    private String schema;

    @ApiModelProperty("模式数据")
    private List<Map<String,String>> schemaList;
}
