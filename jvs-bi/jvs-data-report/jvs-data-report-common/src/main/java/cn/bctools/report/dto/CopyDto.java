package cn.bctools.report.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
@ApiModel("复制类")
public class CopyDto {

    /**
     * 目录id
     */
    @ApiModelProperty("目录id")
    @NotBlank(message = "未选择目录")
    private String menuId;

    /**
     * 报表新名称
     */
    @ApiModelProperty("报表名称")
    @NotBlank(message = "未设置报表名称")
    private String name;

    /**
     * 报表id
     */
    @ApiModelProperty("报表id")
    @NotBlank(message = "未选择报表")
    private String reportId;
}
