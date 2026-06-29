package cn.bctools.auth.login.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 */
@Data
@Accessors(chain = true)
@ApiModel("LDAP登录")
public class LdapDto {

    /**
     * 登录账号
     */
    String account;

    /**
     * 密码
     */
    String password;
}
