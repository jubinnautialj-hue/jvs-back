package cn.bctools.index.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.database.handler.JsonStrTypeHandler;
import cn.bctools.index.handler.HomeRoleTypeHandler;
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
import java.util.List;

/**
 * <p>
 * 首页数据
 * </p>
 *
 * @author admin
 * @since 2023-03-16
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@TableName(value = "sys_home_design", autoResultMap = true)
@ApiModel(value = "SysHome对象", description = "首页数据")
public class SysHome extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("首页设计数据")
    @TableField("design_content")
    private String designContent;

    @ApiModelProperty("首页设计数据")
    @TableField("client_code")
    private String clientCode;
    @TableField("sort")
    private Integer sort;

    @ApiModelProperty("首页设计人")
    @TableField("user_id")
    private String userId;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("标题")
    @TableField(typeHandler = HomeRoleTypeHandler.class)
    private List<HomeRole> roles;

    @ApiModelProperty("默认的首页")
    private boolean defaultHome = false;

}
