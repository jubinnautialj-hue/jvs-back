package cn.bctools.bi.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Administrator
 */
@ApiModel("导出数据-图表图片")
@Data
@Accessors(chain = true)
public class ChartBackImgDto {
    @ApiModelProperty("图片minio-url")
    private String minioUrl;
    @ApiModelProperty("图片本地url")
    private String fielUrl;
}
