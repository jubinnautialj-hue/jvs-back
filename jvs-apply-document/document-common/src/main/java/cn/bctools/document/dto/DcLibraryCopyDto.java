package cn.bctools.document.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaohui
 */
@Data
@Accessors(chain = true)
@ApiModel("文档复制入参")
public class DcLibraryCopyDto {
    @ApiModelProperty("源文件id")
    private String id;
    @ApiModelProperty("上级id")
    private String parentId;
    @ApiModelProperty("文件名称")
    private String name;

}
