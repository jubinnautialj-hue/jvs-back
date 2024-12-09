package cn.bctools.design.permission.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.permission.entity.dto.PermissionDto;
import cn.bctools.design.permission.entity.enums.PermissionType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * @author zhuxiaokang
 */

@Data
@Accessors(chain = true)
@ApiModel("权限设置")
@TableName(value = "jvs_permission_setting", autoResultMap = true)
public class PermissionSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("权限组id")
    @TableField("permission_group_id")
    private String permissionGroupId;

    @ApiModelProperty(value = "权限类型")
    @TableField("permission_type")
    private PermissionType permissionType;

    @ApiModelProperty(value = "设计id")
    @TableField("design_id")
    private String designId;

    @ApiModelProperty(value = "权限")
    @TableField(value = "permission", typeHandler = Fastjson2TypeHandler.class)
    private PermissionDto permission;

    @ApiModelProperty("应用ID")
    private String jvsAppId;
}
