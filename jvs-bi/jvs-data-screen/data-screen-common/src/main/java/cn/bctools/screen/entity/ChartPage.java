package cn.bctools.screen.entity;

import cn.bctools.common.enums.SupportedClientType;
import cn.bctools.screen.enums.ChartPageSource;
import cn.bctools.screen.enums.PreviewSettingEnum;
import cn.bctools.screen.model.BaseAuthPo;
import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 页面配置
 *
 * @author zqs
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@TableName(value = "jvs_chart_page", autoResultMap = true)
@ApiModel
public class ChartPage extends BaseAuthPo implements Serializable {

    private static final long serialVersionUID = -5623407547220439967L;

    @ApiModelProperty("id")
    @TableId(type = IdType.ASSIGN_UUID)
    @NotNull(message = "设计不能为空")
    private String id;
    @NotBlank(message = "名称不能为空")
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("支持的客户端类型")
    private SupportedClientType supportedClientType;
    @ApiModelProperty("是否校验登录, 默认为true")
    private Boolean checkLogin;
    @ApiModelProperty("发布状态")
    private Boolean isDeploy;
    @NotBlank(message = "类型不能为空")
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
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Boolean delFlag;
    @ApiModelProperty("固定搜索栏")
    private Boolean isFixed;
    @ApiModelProperty("来源")
    private ChartPageSource source;
/*    @TableField(exist = false)
    @ApiModelProperty("组件导出下级ID")
    List<ChartComponent> chartComponents;*/

    @ApiModelProperty("菜单排序")
    private Integer sort;

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

    /*分享*/
    @ApiModelProperty("是否公开链接")
    private Boolean shareIs;

    @ApiModelProperty("长链接")
    @TableField(updateStrategy =FieldStrategy.IGNORED )
    private String longLink;

    @ApiModelProperty("短链接")
    @TableField(updateStrategy =FieldStrategy.IGNORED )
    private String shortLink;

    @ApiModelProperty("短链接值")
    private String shortLinkValue;

    @ApiModelProperty("密码")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String password;

    @ApiModelProperty("到期时间")
    @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime expirationTime;

    @ApiModelProperty("租户")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("预设尺寸")
    @TableField("screen_size")
    private String screenSize;

    @ApiModelProperty("是否锁定纵横比")
    @TableField("lock_aspect_ratio")
    private Boolean lockAspectRatio;

    @ApiModelProperty("主题")
    @TableField("theme")
    private String theme;

    @ApiModelProperty("是否开启渐变")
    @TableField("use_theme_gradient")
    private Boolean useThemeGradient;

    @ApiModelProperty("前端自定义数据")
    @TableField(value = "custom_json",typeHandler = FastjsonTypeHandler.class)
    private JSONObject customJson;

    @ApiModelProperty("画布")
    @TableField(exist = false)
    List<JvsChartPageCanvas> canvasList;

    @ApiModelProperty("被删除的画布id")
    @TableField(exist = false)
    List<String> deleteCanvasIds;

    public void clear(){
        this.setCreateById(null);
        this.setCreateTime(null);
        this.setUpdateBy(null);
        this.setUpdateTime(null);
        this.setTenantId(null);
        this.setCreateBy(null);
        this.setRoleType(false);
        this.setRole(new ArrayList<>());
        this.setType(null);
        this.setShareIs(false);
        this.setLongLink(null);
        this.setShortLink(null);
        this.setExpirationTime(null);
        this.setShortLinkValue(null);
    }
}
