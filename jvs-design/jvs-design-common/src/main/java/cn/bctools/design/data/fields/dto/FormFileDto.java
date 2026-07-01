package cn.bctools.design.data.fields.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 表单-文件信息
 *
 * @Author: GuoZi
 */
@Data
@ApiModel("表单-文件信息")
public class FormFileDto {

    @ApiModelProperty("文件路径")
    private String fileName;

    @ApiModelProperty("文件名称")
    private String name;

    @ApiModelProperty("上传状态")
    private String status;

    @ApiModelProperty("文件标识")
    private Long uid;

    @ApiModelProperty("文件访问地址")
    private String url;

}
