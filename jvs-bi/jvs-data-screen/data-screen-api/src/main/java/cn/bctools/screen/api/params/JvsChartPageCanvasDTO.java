package cn.bctools.screen.api.params;

import com.alibaba.fastjson.JSONObject;
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
@ApiModel(value = "画布DTO", description = "大屏-画布")
public class JvsChartPageCanvasDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @ApiModelProperty("大屏id")
    private String chartId;

    @ApiModelProperty("画布名称")
    private String canvasName;

    @ApiModelProperty("画布图片")
    private String canvasImg;

    @ApiModelProperty("页面设计")
    private Object dataJson;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("画布类型")
    private Object canvasType;

    @ApiModelProperty("图片")
    private String imgUrl;

    @ApiModelProperty("背景色")
    private String background;

    @ApiModelProperty("高")
    private Double height;

    @ApiModelProperty("宽")
    private Double width;

    @ApiModelProperty("前端自定义数据")
    private JSONObject customJson;
}
