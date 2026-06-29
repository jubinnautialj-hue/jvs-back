package cn.bctools.auth.api.dto;

import cn.bctools.auth.api.enums.EnvironmentVariableEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author jvs
 */
@Data
@Accessors(chain = true)
public class EnvironmentVariableDto {

    @ApiModelProperty("键")
    private String label;
    @ApiModelProperty("环境变量的类型")
    private EnvironmentVariableEnum type;
    @ApiModelProperty("值")
    private Object value;
    @ApiModelProperty("说明")
    private String remark;

}
