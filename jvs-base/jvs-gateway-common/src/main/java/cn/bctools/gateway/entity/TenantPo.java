package cn.bctools.gateway.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author guojing
 * 公司租户管理
 */
@Data
@TableName(value = "sys_tenant", autoResultMap = true)
@Accessors(chain = true)
@ApiModel(value = "公司租户管理")
public class TenantPo {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "公司ID")
    private String id;
    @ApiModelProperty(value = "公司全称")
    private String name;
    @ApiModelProperty(value = "启用禁用,禁用后，此下的用户都不可登录和操作")
    private Boolean enable;
    @ApiModelProperty(value = "上级租户ID")
    private String parentId;
    @ApiModelProperty(value = "修改时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @TableLogic
    @ApiModelProperty(value = "是否删除  1：已删除  0：正常", hidden = true)
    @TableField(select = false)
    private Boolean delFlag;
    @ApiModelProperty(value = "公司简介")
    private String descMsg;
    @ApiModelProperty(value = "管理员ID字段")
    private String adminUserId;
    private String adminUserAccount;
    @ApiModelProperty(value = "管理员名称")
    @TableField(exist = false)
    private String adminUserName;
    @ApiModelProperty(value = "管理员头像")
    @TableField(exist = false)
    private String adminUserImg;
    @ApiModelProperty(value = "用户默认密码", notes = "未设置初始密码，默认123456")
    @TableField(value = "default_password")
    private String defaultPassword = "123456";
    /**
     * 扩展字段
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    Map<String, String> extensionJson;
    /**
     * ICON
     */
    @TableField(exist = false)
    private String icon;
    /**
     * logo
     */
    @TableField(exist = false)
    private String logo;
    @TableField(exist = false)
    private String systemName;
    @TableField(exist = false)
    private String shortName;
}
