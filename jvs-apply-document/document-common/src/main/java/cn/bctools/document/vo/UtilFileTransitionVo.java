package cn.bctools.document.vo;


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
@ApiModel("文件转换入参")
public class UtilFileTransitionVo extends UtilVo {
    @ApiModelProperty("输出后缀名")
    private String outSuffix;
}
