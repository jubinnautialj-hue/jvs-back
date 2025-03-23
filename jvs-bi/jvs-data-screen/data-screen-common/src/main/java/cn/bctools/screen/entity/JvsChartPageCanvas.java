package cn.bctools.screen.entity;

import cn.bctools.database.entity.po.BasalPo;
import cn.bctools.screen.enums.CanvasTypeEnum;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 大屏-画布
 * </p>
 *
 * @author admin
 * @since 2023-08-18
 */
@Data
@Accessors(chain = true)
@TableName(value = "jvs_chart_page_canvas",autoResultMap = true)
@ApiModel(value = "JvsChartPageCanvas对象", description = "大屏-画布")
public class JvsChartPageCanvas extends BasalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("id")
    private String id;

    @ApiModelProperty("大屏id")
    @TableField("chart_id")
    private String chartId;

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

    @ApiModelProperty("所属租户")
    @TableField("tenant_id")
    private String tenantId;

    @ApiModelProperty("画布类型")
    private CanvasTypeEnum canvasType;

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

    @ApiModelProperty("前端自定义数据")
    @TableField(value = "custom_json",typeHandler = FastjsonTypeHandler.class)
    private JSONObject customJson;


    public void clear(){
        this.setCreateById(null);
        this.setCreateTime(null);
        this.setUpdateBy(null);
        this.setUpdateTime(null);
        this.setTenantId(null);
        this.setCreateBy(null);
    }
}
