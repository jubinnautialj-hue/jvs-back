package cn.bctools.auth.login.dto;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author zhuxiaokang
 * 客户自有系统登录（定制登录）用户信息
 */
@Data
@Accessors(chain = true)
@ApiModel("客户自有系统登录（定制登录）用户信息")
public class OwnUserDao {

    /**
     * 客户自有系统用户唯一id
     */
    private String uniqueId;

    /**
     * 用户名
     */
    private String realName;

    /**
     * 账号
     */
    private String account;

    /**
     * 自定义接入标识
     */
    private String customizedFlag;

    /**
     * 扩展信息
     */
    private JSONObject extension;
}
