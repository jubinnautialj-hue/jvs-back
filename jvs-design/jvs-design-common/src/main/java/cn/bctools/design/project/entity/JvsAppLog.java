package cn.bctools.design.project.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import cn.bctools.design.project.entity.enums.AppLogTypeEnum;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author zxk
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("应用日志")
@TableName(value = "jvs_app_log", autoResultMap = true)
public class JvsAppLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @ApiModelProperty("操作内容")
    @TableField(value = "description", typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> description;
    @ApiModelProperty("应用id")
    private String jvsAppId;
    @ApiModelProperty("应用名")
    private String jvsAppName;
    @ApiModelProperty("用户名")
    private String userId;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("部门id")
    private String deptId;
    @ApiModelProperty("部门名称")
    private String deptName;
    @ApiModelProperty("帐号")
    private String account;
    @ApiModelProperty("模型id")
    private String modeId;
    @ApiModelProperty("模型名称")
    private String modelName;
    @ApiModelProperty("角色名")
    @TableField(value = "roles", typeHandler = Fastjson2TypeHandler.class)
    private List<String> roles;
    @ApiModelProperty("操作类型（新增，修改，删除，导出，）")
    private AppLogTypeEnum type;
    @ApiModelProperty("变更后的数据")
    @TableField(value = "after_json", typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> afterJson;
    @ApiModelProperty("变更前的数据")
    @TableField(value = "before_json", typeHandler = Fastjson2TypeHandler.class)
    private Map<String, Object> beforeJson;
    @ApiModelProperty("创建人")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("对应的设计id")
    private String designId;
    @ApiModelProperty("按钮名称")
    private String buttonName;

}
