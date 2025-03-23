package cn.bctools.screen.entity;

import cn.bctools.database.entity.po.BasePo;
import cn.bctools.screen.enums.PreviewSettingEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 页面配置
 *
 * @author zqs
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName(value = "jvs_chart_template", autoResultMap = true)
@ApiModel
public class ChartTemplate extends BasePo implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    @NotNull(message = "设计不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("组件json")
    private String dataJson;
    @ApiModelProperty("图标")
    private String icon;
    @ApiModelProperty("封面桶名称")
    @Size(max = 500,message = "图片地址过长")
    private String coverFilePath;
    @ApiModelProperty("封面文件路径")
    private String coverBucketName;
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty("封面url")
    @TableField(exist = false)
    private String coverUrl;

    @ApiModelProperty("图片")
    @TableField("img_url")
    private String imgUrl;

    @ApiModelProperty("背景色")
    @TableField("background")
    private String background;

    @ApiModelProperty("高")
    @TableField("height")
    private Double height;

    @ApiModelProperty("宽")
    @TableField("width")
    private Double width;

    @ApiModelProperty("预览设置")
    @TableField("preview_setting")
    private PreviewSettingEnum previewSetting;

    @ApiModelProperty("临时素材")
    @TableField("custom_material")
    private String customMaterial;

    @ApiModelProperty("预设尺寸")
    @TableField("screen_size")
    private String screenSize;

    @ApiModelProperty("是否锁定纵横比")
    @TableField("lock_aspect_ratio")
    private Boolean lockAspectRatio;

    @ApiModelProperty("前端自定义数据")
    @TableField(value = "custom_json",typeHandler = FastjsonTypeHandler.class)
    private JSONObject customJson;

    @ApiModelProperty("画布列表")
    @TableField(exist = false)
    private List<JvsChartPageCanvas> canvasList;

    @ApiModelProperty("被删除的画布id")
    @TableField(exist = false)
    List<String> deleteCanvasIds;
}
