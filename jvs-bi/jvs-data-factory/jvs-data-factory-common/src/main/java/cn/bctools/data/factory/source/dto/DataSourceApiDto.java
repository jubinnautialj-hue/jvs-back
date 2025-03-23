package cn.bctools.data.factory.source.dto;

import cn.bctools.data.factory.source.data.po.InParameterJsonPo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author admin
 */
@Data
@ApiModel("数据源api请求入参")
@Accessors(chain = true)
public class DataSourceApiDto {
    @ApiModelProperty("数据源id")
    private String dataSourceId;
    @ApiModelProperty("接口id")
    private String interfaceId;
    @ApiModelProperty("数据源类型")
    private String sourceType;
    @ApiModelProperty("入参")
    private List<InParameterJsonPo> inParameterJson;
    @ApiModelProperty("表结构id-sql方式没有")
    private String id;
    @ApiModelProperty("当前页码")
    private Long current;
    @ApiModelProperty("需要查询的条数 如果为0表示查询所有")
    private Long size;
//    @ApiModelProperty("请求来源")
//    private RequestSourceDto requestSourceDto;
}
