package cn.bctools.document.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文件同步")
public class FileSyncDto {
    @ApiModelProperty("文件夹名称")
    @NotBlank(message = "未选择同步文件夹")
    private String folderName;
    @ApiModelProperty("知识库id或者文件夹id")
    private String dcLibraryId;
    @ApiModelProperty("本次同步标识")
    @NotBlank(message = "未设置同步标识")
    private String syncIndicator;
}
