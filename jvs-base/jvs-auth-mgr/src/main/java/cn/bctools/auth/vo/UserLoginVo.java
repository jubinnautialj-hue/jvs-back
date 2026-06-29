package cn.bctools.auth.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginVo {

    /**用户昵称*/
    private String realName;
    /**用户邮件*/
    private String email;
    /**
     * 用户名，默认为登录用户名
     */
    private String accountName;
    /**头像*/
    private String headImg;
    /**手机号*/
    private String phone;
    @ApiModelProperty("应用id")
    String jvs;
    /**访问ip*/
    private String ip;
    /**
     * 记录登录终端
     */
    private String clientId;
    /**浏览器头*/
    private String userAgent;

}
