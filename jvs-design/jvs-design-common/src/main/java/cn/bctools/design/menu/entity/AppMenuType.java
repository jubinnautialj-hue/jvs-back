package cn.bctools.design.menu.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 轻应用菜单目录
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("jvs_app_menu_type")
public class AppMenuType implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("唯一标识")
    private String id;

    @ApiModelProperty("目录名")
    @TableField("type")
    private String type;

    @ApiModelProperty("图标")
    @TableField("icon")
    private String icon;

    @ApiModelProperty("排序")
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty(value = "上级目录id", notes = "顶级目录的上级目录id一定为null")
    @TableField(value = "parent_id", updateStrategy = FieldStrategy.IGNORED)
    private String parentId;

    @ApiModelProperty("应用")
    @TableField("jvs_app_id")
    private String jvsAppId;

    @ApiModelProperty("轻应用版本号")
    private String appVersion;

}
