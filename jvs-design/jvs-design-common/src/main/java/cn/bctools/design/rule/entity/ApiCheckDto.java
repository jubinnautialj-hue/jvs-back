package cn.bctools.design.rule.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author jvs
 * api调用逻辑时，的校验规则，ip白名单，或自定义凭证规则
 */
@Data
@Accessors(chain = true)
@ApiModel("api调用逻辑校验规则")
public class ApiCheckDto implements Serializable {

    @ApiModelProperty("ip,使用逗号分割")
    private String ips;
    @ApiModelProperty("用户自定义校验标识")
    private String accessToken;
}
