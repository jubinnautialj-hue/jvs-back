package cn.bctools.chart.entity;

import cn.bctools.database.entity.po.BasalPo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.management.relation.Role;
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
public class SysMenu extends RolePo implements Serializable {

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
    @ApiModelProperty("排序")
    private Long sort;

    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty("子菜单")
    @TableField(exist = false)
    private List<ChartPage> children;
    @ApiModelProperty("此菜单的子集数据量方便前端按钮显示")
    @TableField(exist = false)
    private Long count;

}
