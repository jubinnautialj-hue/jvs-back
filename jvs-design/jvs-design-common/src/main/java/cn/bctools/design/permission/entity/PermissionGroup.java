package cn.bctools.design.permission.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.permission.entity.dto.PermissionMemberDto;
import cn.bctools.design.permission.entity.enums.PermissionGroupType;
import cn.bctools.design.permission.entity.enums.PermissionType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * @author zhuxiaokang
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@ApiModel("权限组")
@TableName(value = "jvs_permission_group", autoResultMap = true)
public class PermissionGroup extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty(value = "权限组名")
    @TableField("group_name")
    private String groupName;

    @ApiModelProperty(value = "组类型")
    @TableField("group_type")
    private PermissionGroupType groupType;

    @ApiModelProperty(value = "权限类型")
    @TableField("permission_type")
    private PermissionType permissionType;


    @ApiModelProperty(value = "权限成员")
    @TableField(value = "member", typeHandler = Fastjson2TypeHandler.class)
    private PermissionMemberDto member;

    @ApiModelProperty("应用ID")
    private String jvsAppId;

}
