package cn.bctools.data.factory.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author admin
 */
@Data
@ApiModel("数据etl")
@Accessors(chain = true)
public class RequestDetailDto {
    @ApiModelProperty("应用名称")
    private String applyName;
    @ApiModelProperty("当前页码")
    private Long current;
    @ApiModelProperty("需要查询的条数 如果为0表示查询所有")
    private Long size;
    @ApiModelProperty("智仓id")
    private String dataFactoryId;
    @ApiModelProperty("用户设计的名称-例如来源图表-图表设计的名称")
    private String designName;
    @ApiModelProperty("用户设计的id")
    private String designId;
    @ApiModelProperty("来源类型")
    private RequestTypeEnum type;
    @ApiModelProperty("资源id-二级数据id")
    private String designDetailId;
    @ApiModelProperty("资源名称-二级名称")
    private String designDetailName;
}
