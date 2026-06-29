package cn.bctools.document.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@ApiModel("工具ocr入参")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UtilOCRVo extends UtilVo {
    @ApiModelProperty("是否提取纯文本")
    @NotNull(message = "是否提取纯文本不能为空")
    private Boolean isGetText;
}
