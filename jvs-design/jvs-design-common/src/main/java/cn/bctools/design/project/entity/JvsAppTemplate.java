package cn.bctools.design.project.entity;

import cn.bctools.database.handler.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Auto Generator
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("应用模板")
@TableName(value = "jvs_app_template", autoResultMap = true)
public class JvsAppTemplate implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty("主键")
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;
    @TableField(fill = FieldFill.INSERT)
    private String createById;
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    @ApiModelProperty("应用名称")
    private String name;
    @ApiModelProperty("分类")
    private String type;
    @ApiModelProperty("应用图标")
    @TableField(value = "app_icon")
    private String appIcon;
    @ApiModelProperty("版本应用的版本,前端传递  如果不一样的版本号，可能导致无法使用")
    private String version;
    @ApiModelProperty("LOGO")
    private String logo;
    @ApiModelProperty("banner")
    private String banner;
    @ApiModelProperty("描述图片")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<String> imgs;
    @ApiModelProperty("应用数据")
    private String data;
    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("类型图标，设计里面每新增一个应用就保存一个")
    @TableField(value = "icon", typeHandler = Fastjson2TypeHandler.class)
    private List<String> icon;

    @ApiModelProperty("类型，设计里面每新增一个应用就保存一个")
    @TableField(typeHandler = Fastjson2TypeHandler.class)
    private List<String> types;

    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("模板大小")
    @TableField("template_size")
    private Integer size;
    @ApiModelProperty("排序")
    private Integer sort;
    @ApiModelProperty("是否免费")
    @TableField("template_free")
    private Boolean free;
    @ApiModelProperty("是否原生应用")
    private Boolean primitive;
    @ApiModelProperty("平台名称")
    private String platform;
    @ApiModelProperty("详细描述")
    private String longText;
    @ApiModelProperty("简约说明")
    private String briefDescription;
    @ApiModelProperty("价格")
    private Integer price;
    @ApiModelProperty("发布状态，管理员确定后才能进行发布")
    private Boolean deploy;
    @ApiModelProperty("是否推荐")
    private Boolean recommend;
    @ApiModelProperty(value = "false-非应用版本模板，true-应用版本模板")
    private Boolean versionTemplate;
    @ApiModelProperty("源文件名称")
    private String fileName;
    @ApiModelProperty("桶名称")
    private String bucketName;

    @ApiModelProperty(value = "是否将数据发布到模板")
    @TableField(exist = false)
    private Boolean deployData;

    @ApiModelProperty(value = "要将数据发布到模板的模型id集合")
    @TableField(exist = false)
    private List<String> deployDataModelIds;

}
