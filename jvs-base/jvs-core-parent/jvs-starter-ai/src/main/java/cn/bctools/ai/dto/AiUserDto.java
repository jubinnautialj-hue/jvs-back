package cn.bctools.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * @author jvs
 */
@Data
@FieldNameConstants
@Schema(description = "用户信息")
@Accessors(chain = true)
public class AiUserDto implements Serializable {

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
}
