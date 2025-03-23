package cn.bctools.design.crud.entity;

import cn.bctools.database.entity.po.BasePo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.crud.entity.enums.DataRoleTypeEnum;
import cn.bctools.design.data.fields.dto.page.LayoutEnum;
import cn.bctools.design.data.fields.enums.DesignType;
import cn.bctools.design.menu.entity.AppMenu;
import cn.bctools.design.project.handler.Design;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * <列表页设计>
 *
 * @author auto
 **/
@Design(DesignType.page)
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
@Accessors(chain = true)
@TableName(value = "jvs_crud_page", autoResultMap = true)
public class CrudPage extends BasePo implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("唯一标识")
    @NotNull(message = "设计不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("列表名称")
    private String name;
    @ApiModelProperty("布局类型")
    private LayoutEnum displayType;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("设计数据")
    private String viewJson;
    @ApiModelProperty("是否已发布")
    private Boolean isDeploy;
    @ApiModelProperty("应用ID")
    private String jvsAppId;
    @ApiModelProperty("数据模型ID")
    private String dataModelId;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty(value = "是否跳过数据权限")
    private Boolean stepDataPermission;
    @ApiModelProperty("数据权限")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private JSONArray dataRole;

    public List<DesignRole> getDataRoles() {
        return JSONArray.parseArray(JSONObject.toJSONString(this.getDataRole()), DesignRole.class);
    }

    @ApiModelProperty(value = "指定使用的数据权限类型")
    private DataRoleTypeEnum dataRoleType;


    @TableField(exist = false)
    @NotBlank(message = "类型不能为空")
    @ApiModelProperty("分类")
    private String type;

    @TableField(exist = false)
    @ApiModelProperty("菜单")
    private AppMenu appMenu;

    @ApiModelProperty("轻应用版本号")
    private String appVersion;
}
