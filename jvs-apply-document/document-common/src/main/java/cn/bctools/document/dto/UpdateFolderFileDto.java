package cn.bctools.document.dto;

import cn.bctools.oss.dto.BaseFile;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : admin
 */
@Data
@Accessors(chain = true)
@ApiModel("文件夹上传的入参")
public class UpdateFolderFileDto {
    @ApiModelProperty("上传的唯一key")
    private String key;
    @ApiModelProperty("上级id")
    private String parentId;
    @ApiModelProperty("文件路径")
    private String filePath;
    @ApiModelProperty("文件上传成功后的文件信息")
    private BaseFile baseFile;
    @ApiModelProperty("文件名称")
    private String fileName;
    @ApiModelProperty("是否为覆盖模式")
    private Boolean cover;
}
