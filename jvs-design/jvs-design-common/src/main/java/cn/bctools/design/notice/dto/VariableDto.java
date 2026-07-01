package cn.bctools.design.notice.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author zhuxiaokang
 * 变量
 */
@Data
@Accessors(chain = true)
@ApiModel("变量")
public class VariableDto implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("值")
    private String value;
}
