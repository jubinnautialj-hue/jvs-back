package cn.bctools.design.menu.entity;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.crud.entity.DesignRole;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.menu.entity.dto.PermissionIdentificationDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author zhuxiaokang
 * 菜单权限表
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName(value = "jvs_app_menu", autoResultMap = true)
public class AppMenu extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("唯一标识")
    private String id;

    @ApiModelProperty("设计类型")
    @TableField("design_type")
    private DesignType designType;

    @ApiModelProperty("具体的设计id")
    @TableField("design_id")
    private String designId;

    @ApiModelProperty("应用")
    @TableField("jvs_app_id")
    private String jvsAppId;

    @ApiModelProperty("设计名称")
    @TableField("name")
    private String name;

    @ApiModelProperty("所属目录")
    @TableField("type")
    private String type;

    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty("移动端显示")
    @TableField("mobile_display")
    private Boolean mobileDisplay;

    @ApiModelProperty("PC端显示")
    @TableField("pc_display")
    private Boolean pcDisplay;

    @ApiModelProperty("权限类型")
    @TableField("role_type")
    private Boolean roleType;

    @ApiModelProperty("权限集,前端设计的,  单个设计中设置了权限后.优先使用角色权限, 后续,将遗弃此字段,不做权限判断,统一使用角色 授权.")
    @TableField(value = "role", typeHandler = Fastjson2TypeHandler.class)
    private JSONArray role;

    public List<DesignRole> getRoles() {
        if (ObjectNull.isNull(this.getRole())) {
            return Collections.emptyList();
        }
        return JSON.parseArray(JSONObject.toJSONString(this.getRole()), DesignRole.class);
    }

    @ApiModelProperty("图标")
    @TableField("icon")
    private String icon;

    @ApiModelProperty(value = "设计相关的资源,保存设计时,更新此字段即可", notes = "2.1.8")
    @TableField(value = "permission_json", typeHandler = Fastjson2TypeHandler.class)
    private JSONArray permissionJson;

    @ApiModelProperty(value = "设计相关的资源", notes = "2.1.9")
    @TableField(value = "permission", typeHandler = Fastjson2TypeHandler.class)
    private PermissionIdentificationDto permission;

    @ApiModelProperty("数据模型ID")
    private String dataModelId;

    @ApiModelProperty("轻应用版本号")
    private String appVersion;

}
