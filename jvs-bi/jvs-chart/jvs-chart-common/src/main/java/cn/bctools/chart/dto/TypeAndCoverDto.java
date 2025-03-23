package cn.bctools.chart.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("封面与模板分类")
public class TypeAndCoverDto {
    @ApiModelProperty("封面文件路径")
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
    private String typeId;

}
