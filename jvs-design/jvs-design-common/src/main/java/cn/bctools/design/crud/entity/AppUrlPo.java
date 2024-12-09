package cn.bctools.design.crud.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author guojing
 */

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
@Accessors(chain = true)
@TableName(value = "jvs_app_url", autoResultMap = true)
public class AppUrlPo extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    @ApiModelProperty("唯一标识")
    private String id;
    @ApiModelProperty("应用ID")
    private String jvsAppId;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("页面名称")
    private String name;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("页面链接")
    private String url;
    @ApiModelProperty("权限集,前端设计的,  单个设计中设置了权限后.优先使用角色权限, 后续,将遗弃此字段,不做权限判断,统一使用角色 授权.")
    @TableField(value = "role", typeHandler = Fastjson2TypeHandler.class)
    private JSONArray role;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;

    @TableField(exist = false)
    @ApiModelProperty("分类")
    private String type;
}
