package cn.bctools.design.rule.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * [Description]: 下拉框数据
 *
 * @author : xh
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("下拉框")
public class RuleDto implements Serializable {
    /**
     * 远程调用的key
     */
    @ApiModelProperty("远程调用的key")
    private String key;
    /**
     * 模板名称
     */
    @ApiModelProperty("模板名称")
    private String name;
}
