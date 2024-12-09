package cn.bctools.auth.login.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 客户自有系统登录（定制登录）参数
 */
@Data
@Accessors(chain = true)
@ApiModel("客户自有系统登录（定制登录）")
public class OwnDao {

    @ApiModelProperty(value = "用户信息加密字符串")
    private String code;
}
