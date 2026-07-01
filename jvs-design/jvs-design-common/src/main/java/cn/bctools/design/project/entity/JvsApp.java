package cn.bctools.design.project.entity;

import cn.bctools.auth.api.dto.PersonnelDto;
import cn.bctools.auth.api.enums.PersonnelTypeEnum;
import cn.bctools.database.entity.po.BasePo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.project.entity.dto.AppRoleDto;
import cn.bctools.design.project.entity.dto.AppTaskDto;
import cn.bctools.oauth2.utils.UserCurrentUtils;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("应用")
@EqualsAndHashCode(callSuper = true)
@TableName(value = "jvs_app", autoResultMap = true)
public class JvsApp extends BasePo implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("应用名称")
    private String name;
    @ApiModelProperty("应用凭证")
    private String secret;
    @ApiModelProperty("分类")
    private String type;
    @ApiModelProperty("LOGO")
    private String logo;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty(value = "发布状态", notes = "发布状态，发布后会创建真实菜单,并在菜单上赋予某个角色,下级目录和 功能 都是挂载到应用下的,如果此应用下线，所有的菜单都会下线")
    private Boolean isDeploy;
    @ApiModelProperty("描述图片")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<String> imgs;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @TableField(exist = false)
    @ApiModelProperty("应用数据")
    private String data;
    @ApiModelProperty("详细描述数据")
    private String longText;
    @ApiModelProperty("是否免费")
    @TableField("app_free")
    private Boolean free;
    @ApiModelProperty("平台名称")
    private String platform;
    @ApiModelProperty("价格")
    private Integer price;
    @ApiModelProperty("排序")
    private Integer sort;
    @ApiModelProperty("应用安装的时候授权的key,此key 有安装时间，是否免费。平台名称，")
    private String authorizationKey;
    @ApiModelProperty("权限设置")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private AppRoleDto role;
    @ApiModelProperty("是否启用轻应用版本功能")
    private Boolean enableVersionFeature;
    @ApiModelProperty("使用的应用版本号")
    private String useVersion;
    @ApiModelProperty("是否推荐")
    private Boolean recommend;
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private AppTaskDto taskSetting;

    /**
     * 创建应用时，设置默认权限
     */
    public void setDefaultRole() {
        // 默认将应用创建人设置为应用主管理员
        this.role = new AppRoleDto()
                .setAdminMember(Collections.singletonList(
                        new PersonnelDto()
                                .setId(UserCurrentUtils.getUserId())
                                .setName(UserCurrentUtils.getRealName())
                                .setType(PersonnelTypeEnum.user)
                ));
    }
}
