package cn.bctools.screen.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.permission.dto.AuthRole;
import cn.bctools.permission.enums.OperationType;
import cn.bctools.permission.handler.ListAuthRoleTypeHandler;
import cn.bctools.screen.model.BaseAuthPo;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 一级菜单
 *
 * @author zqs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "sys_menu", autoResultMap = true)
@ApiModel
public class SysMenu extends BaseAuthPo implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;
    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    @NotNull(message = "设计不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("图标")
    private String icon;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("子菜单")
    @TableField(exist = false)
    private List<ChartPage> children;

}
