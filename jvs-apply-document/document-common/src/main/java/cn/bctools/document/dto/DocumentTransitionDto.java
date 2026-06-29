package cn.bctools.document.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文档转换返回值")
public class DocumentTransitionDto {
    @ApiModelProperty("html内容")
    private String content;
    @ApiModelProperty("转换状态")
    private Boolean status;
    @ApiModelProperty("报错原因")
    private String errorMessage;
}
