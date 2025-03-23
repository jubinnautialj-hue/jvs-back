package cn.bctools.screen.dto;

import cn.bctools.screen.entity.JvsChartPageCanvas;
import cn.bctools.screen.enums.PreviewSettingEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("封面与模板分类")
public class TypeAndCoverDto {
    @ApiModelProperty("封面文件路径")
    @Size(max = 500,message = "图片地址过长")
    private String coverFilePath;
    @ApiModelProperty("封面桶名称")
    private String coverBucketName;
    @ApiModelProperty("设计数据")
    private String dataJson;
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("模板名称")
    private String name;
    @ApiModelProperty("分类id")
    @NotBlank(message = "未选择分类")
    private String typeId;
    @ApiModelProperty("服务")
    private String req;
    @ApiModelProperty("高")
    private Double height;
    @ApiModelProperty("宽")
    private Double width;
    @ApiModelProperty("背景色")
    private String background;
    @ApiModelProperty("背景图片")
    private String imgUrl;

    @ApiModelProperty("预览设置")
    private PreviewSettingEnum previewSetting;

    @ApiModelProperty("画布列表")
    private List<JvsChartPageCanvas> canvasList;

    @ApiModelProperty("临时素材")
    private String customMaterial;

    @ApiModelProperty("预设尺寸")
    private String screenSize;

    @ApiModelProperty("是否锁定纵横比")
    private Boolean lockAspectRatio;

    @ApiModelProperty("前端自定义数据")
    private JSONObject customJson;


}
