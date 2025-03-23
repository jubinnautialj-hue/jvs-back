package cn.bctools.design.rule.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author jvs
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class BodyInDto extends ParameterMap implements Serializable {

    @ApiModelProperty("下级")
    private List<BodyInDto> children;

}
