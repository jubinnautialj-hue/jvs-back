package cn.bctools.document.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 变量与内容
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@ApiModel("变量与内容")
@AllArgsConstructor
@NoArgsConstructor
public class VariablesAndContentDto {
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("变量")
    private List<String> list;

}
