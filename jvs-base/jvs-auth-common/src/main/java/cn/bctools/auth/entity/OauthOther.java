package cn.bctools.auth.entity;

import cn.bctools.auth.login.auth.other.OtherAuthUser;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * The type Oauth other.
 *
 * @author xh
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(value = "sys_oauth_other", autoResultMap = true)
@ApiModel("其它登陆")
public class OauthOther implements Serializable {
    /**
     * The Serial version uid.
     */
    static final long serialVersionUID = 1L;
    /**
     * The Id.
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    String id;
    /**
     * 平台名称
     */
    String name;
    /**
     * 三方平台类型,用于区分不同三方的登陆操作, 和记录登陆日志
     */
    String type;
    /**
     * 客户端id：对应各平台的appKey
     */
    String clientId;

    /**
     * 客户端Secret：对应各平台的appSecret
     */
    String clientSecret;
    /**
     * 图标地址
     */
    String iconUrl;
    @TableField(exist = false)
    String logo;

    public String getLogo() {
        return iconUrl;
    }

    /**
     * 登录成功后的回调地址
     */
    String redirectUri;
    /**
     * 授权的api
     *
     * @return url
     */
    String authorize;

    /**
     * 获取accessToken的api
     *
     * @return url
     */
    String accessToken;
    /**
     * The Logout uri.
     */
    String logoutUri;
    /**
     * 获取用户信息的api
     *
     * @return url
     */
    String userInfo;
    /**
     * 字段关系json
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    OtherAuthUser filedJson;
    /**
     * 扩展字段
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    Map<String, String> extensionJson;
    /**
     * 部门字段映射关系
     */
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    Dept deptJson;
    /**
     * 同步组织结构的接口地址 组织同步接口地址
     */
    String deptUrl;
    /**
     * 拉取用户接口
     */
    String pullUserUrlMethod;
    /**
     * 是否启用的默认登录方式
     */
    Boolean enableDefault;
    /**
     * 租户信息
     */
    String tenantId;

}
