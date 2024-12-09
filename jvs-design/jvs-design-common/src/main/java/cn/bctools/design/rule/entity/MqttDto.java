package cn.bctools.design.rule.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@ApiModel("执行逻辑mq")
public class MqttDto implements Serializable {

    @ApiModelProperty("服务地址")
    private String serverURI;
    @ApiModelProperty("帐号")
    private String userName;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("topic")
    private String topic;
    @ApiModelProperty("qos")
    private Integer qos;
}
