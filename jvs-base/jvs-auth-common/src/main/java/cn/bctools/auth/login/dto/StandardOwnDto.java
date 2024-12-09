package cn.bctools.auth.login.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 标准登录接入
 */
@Data
@Accessors(chain = true)
@ApiModel("客户自有系统登录（标准登录接入）")
public class StandardOwnDto {
    @ApiModelProperty(value = "用户身份标识")
    private String code;
}
