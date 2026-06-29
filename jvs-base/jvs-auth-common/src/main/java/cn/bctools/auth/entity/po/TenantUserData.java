package cn.bctools.auth.entity.po;

import cn.bctools.auth.entity.UserTenant;
import cn.bctools.auth.entity.enums.SexTypeEnum;
import cn.bctools.auth.entity.enums.UserTypeEnum;
import cn.bctools.auth.entity.handler.DeptJsonTypeHandler;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author zhuxiaokang
 * 租户用户信息
 */
@Data
@Accessors(chain = true)
public class TenantUserData extends UserTenant implements Serializable {


    private static final long serialVersionUID = 1L;

    /**
     * 用户基本信息
     */
    @ApiModelProperty(value = "邮件")
    private String email;
    @ApiModelProperty(value = "性别 [male,female]")
    private String sex;
    @ApiModelProperty(value = "登录帐号名全库唯一")
    private String accountName;
    @ApiModelProperty(value = "头像")
    private String headImg;
    @ApiModelProperty(value = "邀请用户id")
    private String invite;
    @ApiModelProperty(value = "用户类型，1后端用户2前端用户3其他业务系统用户")
    private UserTypeEnum userType;
    @ApiModelProperty(value = "生日")
    private LocalDate birthday;
    @ApiModelProperty(value = "部门")
    private List<String> deptId;
}
