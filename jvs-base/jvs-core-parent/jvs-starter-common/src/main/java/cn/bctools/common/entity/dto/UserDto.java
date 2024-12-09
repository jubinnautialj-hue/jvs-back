package cn.bctools.common.entity.dto;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.common.utils.SystemThreadLocal;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 用户基础对象，所有的用户信息都默认都使用此对象进行传递，所有微服务中的登录用户，也都使用此对象进行操作
 *
 * @author gj
 */
@Data
@Accessors(chain = true)
public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 用户主键ID
     */
    private String id;
    /**
     * 用户昵称
     */
    private String realName;
    /**
     * 用户邮件
     */
    private String email;
    /**
     * 用户名，默认为登录用户名
     */
    private String accountName;
    /**
     * 头像
     */
    private String headImg;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 性别
     */
    private String sex;
    /**
     * 岗位id
     */
    private String jobId;
    /**
     * 岗位名称
     */
    private String jobName;
    /**
     * 部门Id
     */
    private String deptId;
    /**
     * 部门编号
     */
    private String deptCode;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 密码
     */
    private String password;
    /**
     * 是否删除
     */
    private Boolean cancelFlag;
    /**
     * 用户邮件
     */
    private String tenantId;
    /**
     * 是否是超级管理员
     */
    private Boolean adminFlag;
    /**
     * 平台级超级管理员
     */
    private Boolean platformAdmin;
    /**
     * 管理员类型
     */
    private List<String> roleType;
    /**
     * 角色id集合
     */
    private List<String> roleIds;
    /**
     * 职工编号
     */
    private String employeeNo;
    /**
     * 用户等级
     */
    private String level;
    /**
     * 浏览器头
     */
    private String userAgent;
    /**
     * 登陆方式
     */
    private String loginType;
    /**
     * 登录的渠道
     */
    private String ch;
    /**
     * 记录登录终端名
     */
    private String clientName;
    /**
     * 用户等级
     */
    private String accountLevel;
    /**
     * 记录登录终端
     */
    private String clientId;

    /**
     * 如果请求头中存在，则使用请求头中的，否则使用自己的
     *
     * @return
     */
    public String getClientId() {
        Object o = SystemThreadLocal.get("clientId");
        if (ObjectNull.isNotNull(o)) {
            return o.toString();
        }
        return clientId;
    }

    /**
     * 三方回调  携带整个用户对象信息
     */
    private String callBackUrl;
    /**
     * 访问ip
     */
    private String ip;
    /**
     * 用户加入的租户
     */
    private List<TenantsDto> tenants;
    @ApiModelProperty("当前选择租户的详细信息")
    private TenantsDto tenant;
    @ApiModelProperty("用户的扩展信息")
    private Map<String, Object> exceptions;

}
