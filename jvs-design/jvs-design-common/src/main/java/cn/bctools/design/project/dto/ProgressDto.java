package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author guojing
 */
@Data
@Accessors(chain = true)
public class ProgressDto {
    String message;
    String errorMessage;
    @ApiModelProperty("执行进度比例")
    private Integer proportion = 0;
    @ApiModelProperty("是否结束")
    Boolean isEnd = false;
}
