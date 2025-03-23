package cn.bctools.data.factory.config;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * mqtt 配置类
 *
 * @author Administrator
 */
@Data
@Accessors(chain = true)
public class MqttProperties {
    @ApiModelProperty("登录密码")
    private String sourcePwd;

    @ApiModelProperty("登录用户名")
    private String sourceUserName;

    @ApiModelProperty("ip地址")
    private String sourceHost;

    @ApiModelProperty("端口")
    private Integer sourcePort;
    @ApiModelProperty("订阅的主题")
    private String subTopic;
}
