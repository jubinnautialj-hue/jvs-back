package cn.bctools.document.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel("版本号提交")
@AllArgsConstructor
@NoArgsConstructor
public class SubVersionsDto {

    @ApiModelProperty("文档id")
    private String dcId;
    @ApiModelProperty("文档内容")
    private String content;
    @ApiModelProperty("版本号用户自定义的")
    private String versionNumber;
}
