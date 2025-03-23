package cn.bctools.auth.component.cons;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author zhuxiaokang
 * 其它平台用户信息
 */
@Data
@Accessors(chain = true)
public class OtherUserDto {
    @ApiModelProperty(value = "头像")
    private String avatar;
    @ApiModelProperty(value = "部门Id",notes = "三方返回的部门Id，可能是数组，也可能是字符串")
    private String deptId;
    @ApiModelProperty(value = "用户姓名", notes = "不指定用户姓名，会随机生成姓名")
    private String userName;
    @ApiModelProperty(value = "邮箱")
    private String email;
    @ApiModelProperty(value = "账号", notes = "不指定账号，会随机生成账号")
    private String accountName;
    @ApiModelProperty(value = "openId", required = true)
    private String openId;
    @ApiModelProperty(value = "手机号")
    private String phone;
    @ApiModelProperty(value = "登录类型", required = true)
    private String loginType;
    @ApiModelProperty(value = "三方平台返回的用户信息")
    private Map<String, Object> otherUser;
}
