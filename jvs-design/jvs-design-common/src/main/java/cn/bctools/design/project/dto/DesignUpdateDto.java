package cn.bctools.design.project.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 应用类型
 *
 * @Author: GuoZi
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DesignUpdateDto extends AppDesignDto {

    @Length(max = 64, message = "设计名称最多64字")
    @ApiModelProperty("设计名称")
    private String name;

    @Length(max = 64, message = "应用目录最多64字")
    @ApiModelProperty("应用目录")
    private String type;

}
