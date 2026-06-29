package cn.bctools.document.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@ApiModel("工具入参")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UtilVo {
    @ApiModelProperty("文件url")
    @NotNull(message = "文件url不能为空")
    private String fileUrl;
    @ApiModelProperty("文件后缀名")
    @NotNull(message = "文件后缀名不能为空")
    private String suffix;
}
