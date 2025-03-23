package cn.bctools.common.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class TenantsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    @ApiModelProperty(value = "默认首页地址")
    private String defaultIndexUrl;
    @ApiModelProperty(value = "职工编号")
    String employeeNo;
    @ApiModelProperty(value = "帐号等级")
    String level;
    @ApiModelProperty(value = "帐号等级id")
    String levelId;
    @ApiModelProperty(value = "岗位ID")
    String jobId;
    @ApiModelProperty(value = "岗位名称")
    String jobName;
    @ApiModelProperty(value = "部门")
    List<DeptDto> dept;
    @ApiModelProperty(value = "租户id")
    String tenantId;

    /**
     * 是否是超级管理员
     */
    private Boolean adminFlag;
    /**
     * 平台级超级管理员
     */
    private Boolean platformAdmin;
    /**
     * 是否顶级租户
     */
    private Boolean topTenant;

    @ApiModelProperty(value = "logo")
    private String logo;
    @ApiModelProperty(value = "租户名称")
    private String shortName;
    private String icon;
    @ApiModelProperty(value = "应用全称")
    private String systemName;
    @ApiModelProperty(value = "管理员ID字段")
    private String adminUserId;
    @ApiModelProperty(value = "管理员名称")
    private String adminUserName;
    @ApiModelProperty(value = "管理员头像")
    private String adminUserImg;
    @ApiModelProperty(value = "登录域名映射")
    private String loginDomain;
}
