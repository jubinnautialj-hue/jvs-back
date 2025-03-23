package cn.bctools.screen.dto;

import cn.bctools.common.enums.SupportedClientType;
import cn.bctools.screen.enums.CanvasTypeEnum;
import cn.bctools.screen.enums.ChartPageSource;
import cn.bctools.screen.enums.PreviewSettingEnum;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class ExportScreenDto {

    private ScreenDto screen;

    private List<CanvasDto> canvasList;

    @Data
    @Accessors(chain = true)
    public static class ScreenDto{

        @ApiModelProperty("名称")
        private String name;
        @ApiModelProperty("支持的客户端类型")
        private SupportedClientType supportedClientType;
        @ApiModelProperty("是否校验登录, 默认为true")
        private Boolean checkLogin;
        @ApiModelProperty("发布状态")
        private Boolean isDeploy;
        @ApiModelProperty("分类")
        private String type;
        @ApiModelProperty("应用ID")
        private String jvsAppId;
        @ApiModelProperty("描述")
        private String description;

        @ApiModelProperty("组件json")
        private String dataJson;
        @ApiModelProperty("筛选条件")
        private String filterJson;
        @ApiModelProperty("图标")
        private String icon;

        @ApiModelProperty("定时请求")
        private String timerRequest;

        @ApiModelProperty("固定搜索栏")
        private Boolean isFixed;

        @ApiModelProperty("来源")
        private ChartPageSource source;

        @ApiModelProperty("菜单排序")
        private Integer sort;

        @ApiModelProperty("图片")
        private String imgUrl;

        @ApiModelProperty("背景色")
        private String background;

        @ApiModelProperty("高")
        private Double height;

        @ApiModelProperty("宽")
        private Double width;

        @ApiModelProperty("预览设置")
        private PreviewSettingEnum previewSetting;

        @ApiModelProperty("临时素材")
        private String customMaterial;
    }

    @Data
    @Accessors(chain = true)
    public static class CanvasDto{

        @ApiModelProperty("画布名称")
        private String canvasName;

        @ApiModelProperty("画布图片")
        private String canvasImg;

        @ApiModelProperty("页面设计")
        @TableField("data_json")
        private String dataJson;

        @ApiModelProperty("排序")
        @TableField("sort")
        private Integer sort;

        @ApiModelProperty("画布类型")
        private CanvasTypeEnum canvasType;

        @ApiModelProperty("图片")
        private String imgUrl;

        @ApiModelProperty("背景色")
        private String background;

        @ApiModelProperty("高")
        private Double height;

        @ApiModelProperty("宽")
        private Double width;
    }
}
